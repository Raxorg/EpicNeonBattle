package com.badlogic.gdx.utils.compression.lzma;

import com.badlogic.gdx.Input.Keys;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.graphics.VertexAttributes.Usage;
import com.badlogic.gdx.utils.compression.ICodeProgress;
import com.badlogic.gdx.utils.compression.lz.BinTree;
import com.badlogic.gdx.utils.compression.rangecoder.BitTreeEncoder;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

public class Encoder {
    public static final int EMatchFinderTypeBT2 = 0;
    public static final int EMatchFinderTypeBT4 = 1;
    static byte[] g_FastPos = new byte[2048];
    static final int kDefaultDictionaryLogSize = 22;
    static final int kIfinityPrice = 268435455;
    static final int kNumFastBytesDefault = 32;
    public static final int kNumLenSpecSymbols = 16;
    static final int kNumOpts = 4096;
    public static final int kPropSize = 5;
    int _additionalOffset;
    int _alignPriceCount;
    int[] _alignPrices = new int[kNumLenSpecSymbols];
    int _dictionarySize = 4194304;
    int _dictionarySizePrev = -1;
    int _distTableSize = 44;
    int[] _distancesPrices = new int[GL20.GL_NEVER];
    boolean _finished;
    InputStream _inStream;
    short[] _isMatch = new short[192];
    short[] _isRep = new short[12];
    short[] _isRep0Long = new short[192];
    short[] _isRepG0 = new short[12];
    short[] _isRepG1 = new short[12];
    short[] _isRepG2 = new short[12];
    LenPriceTableEncoder _lenEncoder = new LenPriceTableEncoder();
    LiteralEncoder _literalEncoder = new LiteralEncoder();
    int _longestMatchLength;
    boolean _longestMatchWasFound;
    int[] _matchDistances = new int[548];
    BinTree _matchFinder = null;
    int _matchFinderType = EMatchFinderTypeBT4;
    int _matchPriceCount;
    boolean _needReleaseMFStream = false;
    int _numDistancePairs;
    int _numFastBytes = kNumFastBytesDefault;
    int _numFastBytesPrev = -1;
    int _numLiteralContextBits = 3;
    int _numLiteralPosStateBits = EMatchFinderTypeBT2;
    Optimal[] _optimum = new Optimal[kNumOpts];
    int _optimumCurrentIndex;
    int _optimumEndIndex;
    BitTreeEncoder _posAlignEncoder = new BitTreeEncoder(4);
    short[] _posEncoders = new short[114];
    BitTreeEncoder[] _posSlotEncoder = new BitTreeEncoder[4];
    int[] _posSlotPrices = new int[Usage.BiNormal];
    int _posStateBits = 2;
    int _posStateMask = 3;
    byte _previousByte;
    com.badlogic.gdx.utils.compression.rangecoder.Encoder _rangeEncoder = new com.badlogic.gdx.utils.compression.rangecoder.Encoder();
    int[] _repDistances = new int[4];
    LenPriceTableEncoder _repMatchLenEncoder = new LenPriceTableEncoder();
    int _state = Base.StateInit();
    boolean _writeEndMark = false;
    int backRes;
    boolean[] finished = new boolean[EMatchFinderTypeBT4];
    long nowPos64;
    long[] processedInSize = new long[EMatchFinderTypeBT4];
    long[] processedOutSize = new long[EMatchFinderTypeBT4];
    byte[] properties = new byte[kPropSize];
    int[] repLens = new int[4];
    int[] reps = new int[4];
    int[] tempPrices = new int[Base.kNumFullDistances];

    class LenEncoder {
        short[] _choice = new short[2];
        BitTreeEncoder _highCoder = new BitTreeEncoder(8);
        BitTreeEncoder[] _lowCoder = new BitTreeEncoder[Encoder.kNumLenSpecSymbols];
        BitTreeEncoder[] _midCoder = new BitTreeEncoder[Encoder.kNumLenSpecSymbols];

        public LenEncoder() {
            for (int posState = Encoder.EMatchFinderTypeBT2; posState < Encoder.kNumLenSpecSymbols; posState += Encoder.EMatchFinderTypeBT4) {
                this._lowCoder[posState] = new BitTreeEncoder(3);
                this._midCoder[posState] = new BitTreeEncoder(3);
            }
        }

        public void Init(int numPosStates) {
            com.badlogic.gdx.utils.compression.rangecoder.Encoder.InitBitModels(this._choice);
            for (int posState = Encoder.EMatchFinderTypeBT2; posState < numPosStates; posState += Encoder.EMatchFinderTypeBT4) {
                this._lowCoder[posState].Init();
                this._midCoder[posState].Init();
            }
            this._highCoder.Init();
        }

        public void Encode(com.badlogic.gdx.utils.compression.rangecoder.Encoder rangeEncoder, int symbol, int posState) throws IOException {
            if (symbol < 8) {
                rangeEncoder.Encode(this._choice, Encoder.EMatchFinderTypeBT2, Encoder.EMatchFinderTypeBT2);
                this._lowCoder[posState].Encode(rangeEncoder, symbol);
                return;
            }
            symbol -= 8;
            rangeEncoder.Encode(this._choice, Encoder.EMatchFinderTypeBT2, Encoder.EMatchFinderTypeBT4);
            if (symbol < 8) {
                rangeEncoder.Encode(this._choice, Encoder.EMatchFinderTypeBT4, Encoder.EMatchFinderTypeBT2);
                this._midCoder[posState].Encode(rangeEncoder, symbol);
                return;
            }
            rangeEncoder.Encode(this._choice, Encoder.EMatchFinderTypeBT4, Encoder.EMatchFinderTypeBT4);
            this._highCoder.Encode(rangeEncoder, symbol - 8);
        }

        public void SetPrices(int posState, int numSymbols, int[] prices, int st) {
            int a0 = com.badlogic.gdx.utils.compression.rangecoder.Encoder.GetPrice0(this._choice[Encoder.EMatchFinderTypeBT2]);
            int a1 = com.badlogic.gdx.utils.compression.rangecoder.Encoder.GetPrice1(this._choice[Encoder.EMatchFinderTypeBT2]);
            int b0 = a1 + com.badlogic.gdx.utils.compression.rangecoder.Encoder.GetPrice0(this._choice[Encoder.EMatchFinderTypeBT4]);
            int b1 = a1 + com.badlogic.gdx.utils.compression.rangecoder.Encoder.GetPrice1(this._choice[Encoder.EMatchFinderTypeBT4]);
            int i = Encoder.EMatchFinderTypeBT2;
            while (i < 8) {
                if (i < numSymbols) {
                    prices[st + i] = this._lowCoder[posState].GetPrice(i) + a0;
                    i += Encoder.EMatchFinderTypeBT4;
                } else {
                    return;
                }
            }
            while (i < Encoder.kNumLenSpecSymbols) {
                if (i < numSymbols) {
                    prices[st + i] = this._midCoder[posState].GetPrice(i - 8) + b0;
                    i += Encoder.EMatchFinderTypeBT4;
                } else {
                    return;
                }
            }
            while (i < numSymbols) {
                prices[st + i] = this._highCoder.GetPrice((i - 8) - 8) + b1;
                i += Encoder.EMatchFinderTypeBT4;
            }
        }
    }

    class LenPriceTableEncoder extends LenEncoder {
        int[] _counters = new int[Encoder.kNumLenSpecSymbols];
        int[] _prices = new int[GL20.GL_DONT_CARE];
        int _tableSize;

        LenPriceTableEncoder() {
            super();
        }

        public void SetTableSize(int tableSize) {
            this._tableSize = tableSize;
        }

        public int GetPrice(int symbol, int posState) {
            return this._prices[(posState * Base.kNumLenSymbols) + symbol];
        }

        void UpdateTable(int posState) {
            SetPrices(posState, this._tableSize, this._prices, posState * Base.kNumLenSymbols);
            this._counters[posState] = this._tableSize;
        }

        public void UpdateTables(int numPosStates) {
            for (int posState = Encoder.EMatchFinderTypeBT2; posState < numPosStates; posState += Encoder.EMatchFinderTypeBT4) {
                UpdateTable(posState);
            }
        }

        public void Encode(com.badlogic.gdx.utils.compression.rangecoder.Encoder rangeEncoder, int symbol, int posState) throws IOException {
            super.Encode(rangeEncoder, symbol, posState);
            int[] iArr = this._counters;
            int i = iArr[posState] - 1;
            iArr[posState] = i;
            if (i == 0) {
                UpdateTable(posState);
            }
        }
    }

    class LiteralEncoder {
        Encoder2[] m_Coders;
        int m_NumPosBits;
        int m_NumPrevBits;
        int m_PosMask;

        class Encoder2 {
            short[] m_Encoders = new short[GL20.GL_SRC_COLOR];

            Encoder2() {
            }

            public void Init() {
                com.badlogic.gdx.utils.compression.rangecoder.Encoder.InitBitModels(this.m_Encoders);
            }

            public void Encode(com.badlogic.gdx.utils.compression.rangecoder.Encoder rangeEncoder, byte symbol) throws IOException {
                int context = Encoder.EMatchFinderTypeBT4;
                for (int i = 7; i >= 0; i--) {
                    int bit = (symbol >> i) & Encoder.EMatchFinderTypeBT4;
                    rangeEncoder.Encode(this.m_Encoders, context, bit);
                    context = (context << Encoder.EMatchFinderTypeBT4) | bit;
                }
            }

            public void EncodeMatched(com.badlogic.gdx.utils.compression.rangecoder.Encoder rangeEncoder, byte matchByte, byte symbol) throws IOException {
                int context = Encoder.EMatchFinderTypeBT4;
                boolean same = true;
                for (int i = 7; i >= 0; i--) {
                    int bit = (symbol >> i) & Encoder.EMatchFinderTypeBT4;
                    int state = context;
                    if (same) {
                        int matchBit = (matchByte >> i) & Encoder.EMatchFinderTypeBT4;
                        state += (matchBit + Encoder.EMatchFinderTypeBT4) << 8;
                        same = matchBit == bit;
                    }
                    rangeEncoder.Encode(this.m_Encoders, state, bit);
                    context = (context << Encoder.EMatchFinderTypeBT4) | bit;
                }
            }

            public int GetPrice(boolean matchMode, byte matchByte, byte symbol) {
                int bit;
                int price = Encoder.EMatchFinderTypeBT2;
                int context = Encoder.EMatchFinderTypeBT4;
                int i = 7;
                if (matchMode) {
                    while (i >= 0) {
                        int matchBit = (matchByte >> i) & Encoder.EMatchFinderTypeBT4;
                        bit = (symbol >> i) & Encoder.EMatchFinderTypeBT4;
                        price += com.badlogic.gdx.utils.compression.rangecoder.Encoder.GetPrice(this.m_Encoders[((matchBit + Encoder.EMatchFinderTypeBT4) << 8) + context], bit);
                        context = (context << Encoder.EMatchFinderTypeBT4) | bit;
                        if (matchBit != bit) {
                            i--;
                            break;
                        }
                        i--;
                    }
                }
                while (i >= 0) {
                    bit = (symbol >> i) & Encoder.EMatchFinderTypeBT4;
                    price += com.badlogic.gdx.utils.compression.rangecoder.Encoder.GetPrice(this.m_Encoders[context], bit);
                    context = (context << Encoder.EMatchFinderTypeBT4) | bit;
                    i--;
                }
                return price;
            }
        }

        LiteralEncoder() {
        }

        public void Create(int numPosBits, int numPrevBits) {
            if (this.m_Coders == null || this.m_NumPrevBits != numPrevBits || this.m_NumPosBits != numPosBits) {
                this.m_NumPosBits = numPosBits;
                this.m_PosMask = (Encoder.EMatchFinderTypeBT4 << numPosBits) - 1;
                this.m_NumPrevBits = numPrevBits;
                int numStates = Encoder.EMatchFinderTypeBT4 << (this.m_NumPrevBits + this.m_NumPosBits);
                this.m_Coders = new Encoder2[numStates];
                for (int i = Encoder.EMatchFinderTypeBT2; i < numStates; i += Encoder.EMatchFinderTypeBT4) {
                    this.m_Coders[i] = new Encoder2();
                }
            }
        }

        public void Init() {
            int numStates = Encoder.EMatchFinderTypeBT4 << (this.m_NumPrevBits + this.m_NumPosBits);
            for (int i = Encoder.EMatchFinderTypeBT2; i < numStates; i += Encoder.EMatchFinderTypeBT4) {
                this.m_Coders[i].Init();
            }
        }

        public Encoder2 GetSubCoder(int pos, byte prevByte) {
            return this.m_Coders[((this.m_PosMask & pos) << this.m_NumPrevBits) + ((prevByte & Keys.F12) >>> (8 - this.m_NumPrevBits))];
        }
    }

    class Optimal {
        public int BackPrev;
        public int BackPrev2;
        public int Backs0;
        public int Backs1;
        public int Backs2;
        public int Backs3;
        public int PosPrev;
        public int PosPrev2;
        public boolean Prev1IsChar;
        public boolean Prev2;
        public int Price;
        public int State;

        Optimal() {
        }

        public void MakeAsChar() {
            this.BackPrev = -1;
            this.Prev1IsChar = false;
        }

        public void MakeAsShortRep() {
            this.BackPrev = Encoder.EMatchFinderTypeBT2;
            this.Prev1IsChar = false;
        }

        public boolean IsShortRep() {
            return this.BackPrev == 0;
        }
    }

    static {
        int c = 2;
        g_FastPos[EMatchFinderTypeBT2] = (byte) 0;
        g_FastPos[EMatchFinderTypeBT4] = (byte) 1;
        for (int slotFast = 2; slotFast < kDefaultDictionaryLogSize; slotFast += EMatchFinderTypeBT4) {
            int k = EMatchFinderTypeBT4 << ((slotFast >> EMatchFinderTypeBT4) - 1);
            int j = EMatchFinderTypeBT2;
            while (j < k) {
                g_FastPos[c] = (byte) slotFast;
                j += EMatchFinderTypeBT4;
                c += EMatchFinderTypeBT4;
            }
        }
    }

    static int GetPosSlot(int pos) {
        if (pos < 2048) {
            return g_FastPos[pos];
        }
        if (pos < 2097152) {
            return g_FastPos[pos >> 10] + 20;
        }
        return g_FastPos[pos >> 20] + 40;
    }

    static int GetPosSlot2(int pos) {
        if (pos < 131072) {
            return g_FastPos[pos >> 6] + 12;
        }
        if (pos < 134217728) {
            return g_FastPos[pos >> kNumLenSpecSymbols] + kNumFastBytesDefault;
        }
        return g_FastPos[pos >> 26] + 52;
    }

    void BaseInit() {
        this._state = Base.StateInit();
        this._previousByte = (byte) 0;
        for (int i = EMatchFinderTypeBT2; i < 4; i += EMatchFinderTypeBT4) {
            this._repDistances[i] = EMatchFinderTypeBT2;
        }
    }

    void Create() {
        if (this._matchFinder == null) {
            BinTree bt = new BinTree();
            int numHashBytes = 4;
            if (this._matchFinderType == 0) {
                numHashBytes = 2;
            }
            bt.SetType(numHashBytes);
            this._matchFinder = bt;
        }
        this._literalEncoder.Create(this._numLiteralPosStateBits, this._numLiteralContextBits);
        if (this._dictionarySize != this._dictionarySizePrev || this._numFastBytesPrev != this._numFastBytes) {
            this._matchFinder.Create(this._dictionarySize, kNumOpts, this._numFastBytes, 274);
            this._dictionarySizePrev = this._dictionarySize;
            this._numFastBytesPrev = this._numFastBytes;
        }
    }

    public Encoder() {
        int i;
        for (i = EMatchFinderTypeBT2; i < kNumOpts; i += EMatchFinderTypeBT4) {
            this._optimum[i] = new Optimal();
        }
        for (i = EMatchFinderTypeBT2; i < 4; i += EMatchFinderTypeBT4) {
            this._posSlotEncoder[i] = new BitTreeEncoder(6);
        }
    }

    void SetWriteEndMarkerMode(boolean writeEndMarker) {
        this._writeEndMark = writeEndMarker;
    }

    void Init() {
        BaseInit();
        this._rangeEncoder.Init();
        com.badlogic.gdx.utils.compression.rangecoder.Encoder.InitBitModels(this._isMatch);
        com.badlogic.gdx.utils.compression.rangecoder.Encoder.InitBitModels(this._isRep0Long);
        com.badlogic.gdx.utils.compression.rangecoder.Encoder.InitBitModels(this._isRep);
        com.badlogic.gdx.utils.compression.rangecoder.Encoder.InitBitModels(this._isRepG0);
        com.badlogic.gdx.utils.compression.rangecoder.Encoder.InitBitModels(this._isRepG1);
        com.badlogic.gdx.utils.compression.rangecoder.Encoder.InitBitModels(this._isRepG2);
        com.badlogic.gdx.utils.compression.rangecoder.Encoder.InitBitModels(this._posEncoders);
        this._literalEncoder.Init();
        for (int i = EMatchFinderTypeBT2; i < 4; i += EMatchFinderTypeBT4) {
            this._posSlotEncoder[i].Init();
        }
        this._lenEncoder.Init(EMatchFinderTypeBT4 << this._posStateBits);
        this._repMatchLenEncoder.Init(EMatchFinderTypeBT4 << this._posStateBits);
        this._posAlignEncoder.Init();
        this._longestMatchWasFound = false;
        this._optimumEndIndex = EMatchFinderTypeBT2;
        this._optimumCurrentIndex = EMatchFinderTypeBT2;
        this._additionalOffset = EMatchFinderTypeBT2;
    }

    int ReadMatchDistances() throws IOException {
        int lenRes = EMatchFinderTypeBT2;
        this._numDistancePairs = this._matchFinder.GetMatches(this._matchDistances);
        if (this._numDistancePairs > 0) {
            lenRes = this._matchDistances[this._numDistancePairs - 2];
            if (lenRes == this._numFastBytes) {
                lenRes += this._matchFinder.GetMatchLen(lenRes - 1, this._matchDistances[this._numDistancePairs - 1], 273 - lenRes);
            }
        }
        this._additionalOffset += EMatchFinderTypeBT4;
        return lenRes;
    }

    void MovePos(int num) throws IOException {
        if (num > 0) {
            this._matchFinder.Skip(num);
            this._additionalOffset += num;
        }
    }

    int GetRepLen1Price(int state, int posState) {
        return com.badlogic.gdx.utils.compression.rangecoder.Encoder.GetPrice0(this._isRepG0[state]) + com.badlogic.gdx.utils.compression.rangecoder.Encoder.GetPrice0(this._isRep0Long[(state << 4) + posState]);
    }

    int GetPureRepPrice(int repIndex, int state, int posState) {
        if (repIndex == 0) {
            return com.badlogic.gdx.utils.compression.rangecoder.Encoder.GetPrice0(this._isRepG0[state]) + com.badlogic.gdx.utils.compression.rangecoder.Encoder.GetPrice1(this._isRep0Long[(state << 4) + posState]);
        }
        int price = com.badlogic.gdx.utils.compression.rangecoder.Encoder.GetPrice1(this._isRepG0[state]);
        if (repIndex == EMatchFinderTypeBT4) {
            return price + com.badlogic.gdx.utils.compression.rangecoder.Encoder.GetPrice0(this._isRepG1[state]);
        }
        return (price + com.badlogic.gdx.utils.compression.rangecoder.Encoder.GetPrice1(this._isRepG1[state])) + com.badlogic.gdx.utils.compression.rangecoder.Encoder.GetPrice(this._isRepG2[state], repIndex - 2);
    }

    int GetRepPrice(int repIndex, int len, int state, int posState) {
        return GetPureRepPrice(repIndex, state, posState) + this._repMatchLenEncoder.GetPrice(len - 2, posState);
    }

    int GetPosLenPrice(int pos, int len, int posState) {
        int price;
        int lenToPosState = Base.GetLenToPosState(len);
        if (pos < Base.kNumFullDistances) {
            price = this._distancesPrices[(lenToPosState * Base.kNumFullDistances) + pos];
        } else {
            price = this._posSlotPrices[(lenToPosState << 6) + GetPosSlot2(pos)] + this._alignPrices[pos & 15];
        }
        return this._lenEncoder.GetPrice(len - 2, posState) + price;
    }

    int Backward(int cur) {
        this._optimumEndIndex = cur;
        int posMem = this._optimum[cur].PosPrev;
        int backMem = this._optimum[cur].BackPrev;
        do {
            if (this._optimum[cur].Prev1IsChar) {
                this._optimum[posMem].MakeAsChar();
                this._optimum[posMem].PosPrev = posMem - 1;
                if (this._optimum[cur].Prev2) {
                    this._optimum[posMem - 1].Prev1IsChar = false;
                    this._optimum[posMem - 1].PosPrev = this._optimum[cur].PosPrev2;
                    this._optimum[posMem - 1].BackPrev = this._optimum[cur].BackPrev2;
                }
            }
            int posPrev = posMem;
            int backCur = backMem;
            backMem = this._optimum[posPrev].BackPrev;
            posMem = this._optimum[posPrev].PosPrev;
            this._optimum[posPrev].BackPrev = backCur;
            this._optimum[posPrev].PosPrev = cur;
            cur = posPrev;
        } while (cur > 0);
        this.backRes = this._optimum[EMatchFinderTypeBT2].BackPrev;
        this._optimumCurrentIndex = this._optimum[EMatchFinderTypeBT2].PosPrev;
        return this._optimumCurrentIndex;
    }

    int GetOptimum(int position) throws IOException {
        if (this._optimumEndIndex != this._optimumCurrentIndex) {
            int lenRes = this._optimum[this._optimumCurrentIndex].PosPrev - this._optimumCurrentIndex;
            this.backRes = this._optimum[this._optimumCurrentIndex].BackPrev;
            this._optimumCurrentIndex = this._optimum[this._optimumCurrentIndex].PosPrev;
            return lenRes;
        }
        int lenMain;
        this._optimumEndIndex = EMatchFinderTypeBT2;
        this._optimumCurrentIndex = EMatchFinderTypeBT2;
        if (this._longestMatchWasFound) {
            lenMain = this._longestMatchLength;
            this._longestMatchWasFound = false;
        } else {
            lenMain = ReadMatchDistances();
        }
        int numDistancePairs = this._numDistancePairs;
        int numAvailableBytes = this._matchFinder.GetNumAvailableBytes() + EMatchFinderTypeBT4;
        if (numAvailableBytes < 2) {
            this.backRes = -1;
            return EMatchFinderTypeBT4;
        }
        int i;
        if (numAvailableBytes > 273) {
        }
        int repMaxIndex = EMatchFinderTypeBT2;
        for (i = EMatchFinderTypeBT2; i < 4; i += EMatchFinderTypeBT4) {
            this.reps[i] = this._repDistances[i];
            this.repLens[i] = this._matchFinder.GetMatchLen(-1, this.reps[i], Base.kMatchMaxLen);
            if (this.repLens[i] > this.repLens[repMaxIndex]) {
                repMaxIndex = i;
            }
        }
        if (this.repLens[repMaxIndex] >= this._numFastBytes) {
            this.backRes = repMaxIndex;
            lenRes = this.repLens[repMaxIndex];
            MovePos(lenRes - 1);
            return lenRes;
        }
        if (lenMain >= this._numFastBytes) {
            this.backRes = this._matchDistances[numDistancePairs - 1] + 4;
            MovePos(lenMain - 1);
            return lenMain;
        }
        byte currentByte = this._matchFinder.GetIndexByte(-1);
        byte matchByte = this._matchFinder.GetIndexByte(((0 - this._repDistances[EMatchFinderTypeBT2]) - 1) - 1);
        if (lenMain >= 2 || currentByte == matchByte || this.repLens[repMaxIndex] >= 2) {
            int shortRepPrice;
            this._optimum[EMatchFinderTypeBT2].State = this._state;
            int posState = position & this._posStateMask;
            this._optimum[EMatchFinderTypeBT4].Price = this._literalEncoder.GetSubCoder(position, this._previousByte).GetPrice(!Base.StateIsCharState(this._state), matchByte, currentByte) + com.badlogic.gdx.utils.compression.rangecoder.Encoder.GetPrice0(this._isMatch[(this._state << 4) + posState]);
            this._optimum[EMatchFinderTypeBT4].MakeAsChar();
            int matchPrice = com.badlogic.gdx.utils.compression.rangecoder.Encoder.GetPrice1(this._isMatch[(this._state << 4) + posState]);
            int repMatchPrice = matchPrice + com.badlogic.gdx.utils.compression.rangecoder.Encoder.GetPrice1(this._isRep[this._state]);
            if (matchByte == currentByte) {
                shortRepPrice = repMatchPrice + GetRepLen1Price(this._state, posState);
                if (shortRepPrice < this._optimum[EMatchFinderTypeBT4].Price) {
                    this._optimum[EMatchFinderTypeBT4].Price = shortRepPrice;
                    this._optimum[EMatchFinderTypeBT4].MakeAsShortRep();
                }
            }
            int lenEnd = lenMain >= this.repLens[repMaxIndex] ? lenMain : this.repLens[repMaxIndex];
            if (lenEnd < 2) {
                this.backRes = this._optimum[EMatchFinderTypeBT4].BackPrev;
                return EMatchFinderTypeBT4;
            }
            int curAndLenPrice;
            Optimal optimum;
            int offs;
            this._optimum[EMatchFinderTypeBT4].PosPrev = EMatchFinderTypeBT2;
            this._optimum[EMatchFinderTypeBT2].Backs0 = this.reps[EMatchFinderTypeBT2];
            this._optimum[EMatchFinderTypeBT2].Backs1 = this.reps[EMatchFinderTypeBT4];
            this._optimum[EMatchFinderTypeBT2].Backs2 = this.reps[2];
            this._optimum[EMatchFinderTypeBT2].Backs3 = this.reps[3];
            int len = lenEnd;
            while (true) {
                int len2 = len - 1;
                this._optimum[len].Price = kIfinityPrice;
                if (len2 < 2) {
                    break;
                }
                len = len2;
            }
            for (i = EMatchFinderTypeBT2; i < 4; i += EMatchFinderTypeBT4) {
                int repLen = this.repLens[i];
                if (repLen >= 2) {
                    int price = repMatchPrice + GetPureRepPrice(i, this._state, posState);
                    do {
                        curAndLenPrice = price + this._repMatchLenEncoder.GetPrice(repLen - 2, posState);
                        optimum = this._optimum[repLen];
                        if (curAndLenPrice < optimum.Price) {
                            optimum.Price = curAndLenPrice;
                            optimum.PosPrev = EMatchFinderTypeBT2;
                            optimum.BackPrev = i;
                            optimum.Prev1IsChar = false;
                        }
                        repLen--;
                    } while (repLen >= 2);
                }
            }
            int normalMatchPrice = matchPrice + com.badlogic.gdx.utils.compression.rangecoder.Encoder.GetPrice0(this._isRep[this._state]);
            len = this.repLens[EMatchFinderTypeBT2] >= 2 ? this.repLens[EMatchFinderTypeBT2] + EMatchFinderTypeBT4 : 2;
            if (len <= lenMain) {
                offs = EMatchFinderTypeBT2;
                while (len > this._matchDistances[offs]) {
                    offs += 2;
                }
                while (true) {
                    int distance = this._matchDistances[offs + EMatchFinderTypeBT4];
                    curAndLenPrice = normalMatchPrice + GetPosLenPrice(distance, len, posState);
                    optimum = this._optimum[len];
                    if (curAndLenPrice < optimum.Price) {
                        optimum.Price = curAndLenPrice;
                        optimum.PosPrev = EMatchFinderTypeBT2;
                        optimum.BackPrev = distance + 4;
                        optimum.Prev1IsChar = false;
                    }
                    if (len == this._matchDistances[offs]) {
                        offs += 2;
                        if (offs == numDistancePairs) {
                            break;
                        }
                    }
                    len += EMatchFinderTypeBT4;
                }
            }
            int cur = EMatchFinderTypeBT2;
            while (true) {
                cur += EMatchFinderTypeBT4;
                if (cur == lenEnd) {
                    return Backward(cur);
                }
                int newLen = ReadMatchDistances();
                numDistancePairs = this._numDistancePairs;
                if (newLen >= this._numFastBytes) {
                    this._longestMatchLength = newLen;
                    this._longestMatchWasFound = true;
                    return Backward(cur);
                }
                int state;
                position += EMatchFinderTypeBT4;
                int posPrev = this._optimum[cur].PosPrev;
                if (this._optimum[cur].Prev1IsChar) {
                    posPrev--;
                    if (this._optimum[cur].Prev2) {
                        state = this._optimum[this._optimum[cur].PosPrev2].State;
                        if (this._optimum[cur].BackPrev2 < 4) {
                            state = Base.StateUpdateRep(state);
                        } else {
                            state = Base.StateUpdateMatch(state);
                        }
                    } else {
                        state = this._optimum[posPrev].State;
                    }
                    state = Base.StateUpdateChar(state);
                } else {
                    state = this._optimum[posPrev].State;
                }
                if (posPrev != cur - 1) {
                    int pos;
                    if (this._optimum[cur].Prev1IsChar && this._optimum[cur].Prev2) {
                        posPrev = this._optimum[cur].PosPrev2;
                        pos = this._optimum[cur].BackPrev2;
                        state = Base.StateUpdateRep(state);
                    } else {
                        pos = this._optimum[cur].BackPrev;
                        if (pos < 4) {
                            state = Base.StateUpdateRep(state);
                        } else {
                            state = Base.StateUpdateMatch(state);
                        }
                    }
                    Optimal opt = this._optimum[posPrev];
                    if (pos >= 4) {
                        this.reps[EMatchFinderTypeBT2] = pos - 4;
                        this.reps[EMatchFinderTypeBT4] = opt.Backs0;
                        this.reps[2] = opt.Backs1;
                        this.reps[3] = opt.Backs2;
                    } else if (pos == 0) {
                        this.reps[EMatchFinderTypeBT2] = opt.Backs0;
                        this.reps[EMatchFinderTypeBT4] = opt.Backs1;
                        this.reps[2] = opt.Backs2;
                        this.reps[3] = opt.Backs3;
                    } else if (pos == EMatchFinderTypeBT4) {
                        this.reps[EMatchFinderTypeBT2] = opt.Backs1;
                        this.reps[EMatchFinderTypeBT4] = opt.Backs0;
                        this.reps[2] = opt.Backs2;
                        this.reps[3] = opt.Backs3;
                    } else if (pos == 2) {
                        this.reps[EMatchFinderTypeBT2] = opt.Backs2;
                        this.reps[EMatchFinderTypeBT4] = opt.Backs0;
                        this.reps[2] = opt.Backs1;
                        this.reps[3] = opt.Backs3;
                    } else {
                        this.reps[EMatchFinderTypeBT2] = opt.Backs3;
                        this.reps[EMatchFinderTypeBT4] = opt.Backs0;
                        this.reps[2] = opt.Backs1;
                        this.reps[3] = opt.Backs2;
                    }
                } else if (this._optimum[cur].IsShortRep()) {
                    state = Base.StateUpdateShortRep(state);
                } else {
                    state = Base.StateUpdateChar(state);
                }
                this._optimum[cur].State = state;
                this._optimum[cur].Backs0 = this.reps[EMatchFinderTypeBT2];
                this._optimum[cur].Backs1 = this.reps[EMatchFinderTypeBT4];
                this._optimum[cur].Backs2 = this.reps[2];
                this._optimum[cur].Backs3 = this.reps[3];
                int curPrice = this._optimum[cur].Price;
                currentByte = this._matchFinder.GetIndexByte(-1);
                matchByte = this._matchFinder.GetIndexByte(((0 - this.reps[EMatchFinderTypeBT2]) - 1) - 1);
                posState = position & this._posStateMask;
                int curAnd1Price = (curPrice + com.badlogic.gdx.utils.compression.rangecoder.Encoder.GetPrice0(this._isMatch[(state << 4) + posState])) + this._literalEncoder.GetSubCoder(position, this._matchFinder.GetIndexByte(-2)).GetPrice(!Base.StateIsCharState(state), matchByte, currentByte);
                Optimal nextOptimum = this._optimum[cur + EMatchFinderTypeBT4];
                boolean nextIsChar = false;
                if (curAnd1Price < nextOptimum.Price) {
                    nextOptimum.Price = curAnd1Price;
                    nextOptimum.PosPrev = cur;
                    nextOptimum.MakeAsChar();
                    nextIsChar = true;
                }
                matchPrice = curPrice + com.badlogic.gdx.utils.compression.rangecoder.Encoder.GetPrice1(this._isMatch[(state << 4) + posState]);
                repMatchPrice = matchPrice + com.badlogic.gdx.utils.compression.rangecoder.Encoder.GetPrice1(this._isRep[state]);
                if (matchByte == currentByte && (nextOptimum.PosPrev >= cur || nextOptimum.BackPrev != 0)) {
                    shortRepPrice = repMatchPrice + GetRepLen1Price(state, posState);
                    if (shortRepPrice <= nextOptimum.Price) {
                        nextOptimum.Price = shortRepPrice;
                        nextOptimum.PosPrev = cur;
                        nextOptimum.MakeAsShortRep();
                        nextIsChar = true;
                    }
                }
                int numAvailableBytesFull = Math.min(4095 - cur, this._matchFinder.GetNumAvailableBytes() + EMatchFinderTypeBT4);
                numAvailableBytes = numAvailableBytesFull;
                if (numAvailableBytes >= 2) {
                    int lenTest2;
                    int state2;
                    int posStateNext;
                    int nextRepMatchPrice;
                    int offset;
                    int lenTest;
                    int curAndLenCharPrice;
                    if (numAvailableBytes > this._numFastBytes) {
                        numAvailableBytes = this._numFastBytes;
                    }
                    if (!(nextIsChar || matchByte == currentByte)) {
                        lenTest2 = this._matchFinder.GetMatchLen(EMatchFinderTypeBT2, this.reps[EMatchFinderTypeBT2], Math.min(numAvailableBytesFull - 1, this._numFastBytes));
                        if (lenTest2 >= 2) {
                            state2 = Base.StateUpdateChar(state);
                            posStateNext = (position + EMatchFinderTypeBT4) & this._posStateMask;
                            nextRepMatchPrice = (com.badlogic.gdx.utils.compression.rangecoder.Encoder.GetPrice1(this._isMatch[(state2 << 4) + posStateNext]) + curAnd1Price) + com.badlogic.gdx.utils.compression.rangecoder.Encoder.GetPrice1(this._isRep[state2]);
                            offset = (cur + EMatchFinderTypeBT4) + lenTest2;
                            while (lenEnd < offset) {
                                lenEnd += EMatchFinderTypeBT4;
                                this._optimum[lenEnd].Price = kIfinityPrice;
                            }
                            curAndLenPrice = nextRepMatchPrice + GetRepPrice(EMatchFinderTypeBT2, lenTest2, state2, posStateNext);
                            optimum = this._optimum[offset];
                            if (curAndLenPrice < optimum.Price) {
                                optimum.Price = curAndLenPrice;
                                optimum.PosPrev = cur + EMatchFinderTypeBT4;
                                optimum.BackPrev = EMatchFinderTypeBT2;
                                optimum.Prev1IsChar = true;
                                optimum.Prev2 = false;
                            }
                        }
                    }
                    int startLen = 2;
                    for (int repIndex = EMatchFinderTypeBT2; repIndex < 4; repIndex += EMatchFinderTypeBT4) {
                        lenTest = this._matchFinder.GetMatchLen(-1, this.reps[repIndex], numAvailableBytes);
                        if (lenTest >= 2) {
                            int lenTestTemp = lenTest;
                            while (true) {
                                if (lenEnd < cur + lenTest) {
                                    lenEnd += EMatchFinderTypeBT4;
                                    this._optimum[lenEnd].Price = kIfinityPrice;
                                } else {
                                    curAndLenPrice = repMatchPrice + GetRepPrice(repIndex, lenTest, state, posState);
                                    optimum = this._optimum[cur + lenTest];
                                    if (curAndLenPrice < optimum.Price) {
                                        optimum.Price = curAndLenPrice;
                                        optimum.PosPrev = cur;
                                        optimum.BackPrev = repIndex;
                                        optimum.Prev1IsChar = false;
                                    }
                                    lenTest--;
                                    if (lenTest < 2) {
                                        break;
                                    }
                                }
                            }
                            lenTest = lenTestTemp;
                            if (repIndex == 0) {
                                startLen = lenTest + EMatchFinderTypeBT4;
                            }
                            if (lenTest < numAvailableBytesFull) {
                                lenTest2 = this._matchFinder.GetMatchLen(lenTest, this.reps[repIndex], Math.min((numAvailableBytesFull - 1) - lenTest, this._numFastBytes));
                                if (lenTest2 >= 2) {
                                    state2 = Base.StateUpdateRep(state);
                                    curAndLenCharPrice = ((GetRepPrice(repIndex, lenTest, state, posState) + repMatchPrice) + com.badlogic.gdx.utils.compression.rangecoder.Encoder.GetPrice0(this._isMatch[(state2 << 4) + ((position + lenTest) & this._posStateMask)])) + this._literalEncoder.GetSubCoder(position + lenTest, this._matchFinder.GetIndexByte((lenTest - 1) - 1)).GetPrice(true, this._matchFinder.GetIndexByte((lenTest - 1) - (this.reps[repIndex] + EMatchFinderTypeBT4)), this._matchFinder.GetIndexByte(lenTest - 1));
                                    state2 = Base.StateUpdateChar(state2);
                                    posStateNext = ((position + lenTest) + EMatchFinderTypeBT4) & this._posStateMask;
                                    nextRepMatchPrice = (curAndLenCharPrice + com.badlogic.gdx.utils.compression.rangecoder.Encoder.GetPrice1(this._isMatch[(state2 << 4) + posStateNext])) + com.badlogic.gdx.utils.compression.rangecoder.Encoder.GetPrice1(this._isRep[state2]);
                                    offset = (lenTest + EMatchFinderTypeBT4) + lenTest2;
                                    while (lenEnd < cur + offset) {
                                        lenEnd += EMatchFinderTypeBT4;
                                        this._optimum[lenEnd].Price = kIfinityPrice;
                                    }
                                    curAndLenPrice = nextRepMatchPrice + GetRepPrice(EMatchFinderTypeBT2, lenTest2, state2, posStateNext);
                                    optimum = this._optimum[cur + offset];
                                    if (curAndLenPrice < optimum.Price) {
                                        optimum.Price = curAndLenPrice;
                                        optimum.PosPrev = (cur + lenTest) + EMatchFinderTypeBT4;
                                        optimum.BackPrev = EMatchFinderTypeBT2;
                                        optimum.Prev1IsChar = true;
                                        optimum.Prev2 = true;
                                        optimum.PosPrev2 = cur;
                                        optimum.BackPrev2 = repIndex;
                                    }
                                }
                            }
                        }
                    }
                    if (newLen > numAvailableBytes) {
                        newLen = numAvailableBytes;
                        numDistancePairs = EMatchFinderTypeBT2;
                        while (newLen > this._matchDistances[numDistancePairs]) {
                            numDistancePairs += 2;
                        }
                        this._matchDistances[numDistancePairs] = newLen;
                        numDistancePairs += 2;
                    }
                    if (newLen >= startLen) {
                        normalMatchPrice = matchPrice + com.badlogic.gdx.utils.compression.rangecoder.Encoder.GetPrice0(this._isRep[state]);
                        while (lenEnd < cur + newLen) {
                            lenEnd += EMatchFinderTypeBT4;
                            this._optimum[lenEnd].Price = kIfinityPrice;
                        }
                        offs = EMatchFinderTypeBT2;
                        while (startLen > this._matchDistances[offs]) {
                            offs += 2;
                        }
                        lenTest = startLen;
                        while (true) {
                            int curBack = this._matchDistances[offs + EMatchFinderTypeBT4];
                            curAndLenPrice = normalMatchPrice + GetPosLenPrice(curBack, lenTest, posState);
                            optimum = this._optimum[cur + lenTest];
                            if (curAndLenPrice < optimum.Price) {
                                optimum.Price = curAndLenPrice;
                                optimum.PosPrev = cur;
                                optimum.BackPrev = curBack + 4;
                                optimum.Prev1IsChar = false;
                            }
                            if (lenTest == this._matchDistances[offs]) {
                                if (lenTest < numAvailableBytesFull) {
                                    lenTest2 = this._matchFinder.GetMatchLen(lenTest, curBack, Math.min((numAvailableBytesFull - 1) - lenTest, this._numFastBytes));
                                    if (lenTest2 >= 2) {
                                        state2 = Base.StateUpdateMatch(state);
                                        curAndLenCharPrice = (com.badlogic.gdx.utils.compression.rangecoder.Encoder.GetPrice0(this._isMatch[(state2 << 4) + ((position + lenTest) & this._posStateMask)]) + curAndLenPrice) + this._literalEncoder.GetSubCoder(position + lenTest, this._matchFinder.GetIndexByte((lenTest - 1) - 1)).GetPrice(true, this._matchFinder.GetIndexByte((lenTest - (curBack + EMatchFinderTypeBT4)) - 1), this._matchFinder.GetIndexByte(lenTest - 1));
                                        state2 = Base.StateUpdateChar(state2);
                                        posStateNext = ((position + lenTest) + EMatchFinderTypeBT4) & this._posStateMask;
                                        nextRepMatchPrice = (curAndLenCharPrice + com.badlogic.gdx.utils.compression.rangecoder.Encoder.GetPrice1(this._isMatch[(state2 << 4) + posStateNext])) + com.badlogic.gdx.utils.compression.rangecoder.Encoder.GetPrice1(this._isRep[state2]);
                                        offset = (lenTest + EMatchFinderTypeBT4) + lenTest2;
                                        while (lenEnd < cur + offset) {
                                            lenEnd += EMatchFinderTypeBT4;
                                            this._optimum[lenEnd].Price = kIfinityPrice;
                                        }
                                        curAndLenPrice = nextRepMatchPrice + GetRepPrice(EMatchFinderTypeBT2, lenTest2, state2, posStateNext);
                                        optimum = this._optimum[cur + offset];
                                        if (curAndLenPrice < optimum.Price) {
                                            optimum.Price = curAndLenPrice;
                                            optimum.PosPrev = (cur + lenTest) + EMatchFinderTypeBT4;
                                            optimum.BackPrev = EMatchFinderTypeBT2;
                                            optimum.Prev1IsChar = true;
                                            optimum.Prev2 = true;
                                            optimum.PosPrev2 = cur;
                                            optimum.BackPrev2 = curBack + 4;
                                        }
                                    }
                                }
                                offs += 2;
                                if (offs == numDistancePairs) {
                                    break;
                                }
                            }
                            lenTest += EMatchFinderTypeBT4;
                        }
                    }
                }
            }
        } else {
            this.backRes = -1;
            return EMatchFinderTypeBT4;
        }
    }

    boolean ChangePair(int smallDist, int bigDist) {
        return smallDist < 33554432 && bigDist >= (smallDist << 7);
    }

    void WriteEndMarker(int posState) throws IOException {
        if (this._writeEndMark) {
            this._rangeEncoder.Encode(this._isMatch, (this._state << 4) + posState, EMatchFinderTypeBT4);
            this._rangeEncoder.Encode(this._isRep, this._state, EMatchFinderTypeBT2);
            this._state = Base.StateUpdateMatch(this._state);
            this._lenEncoder.Encode(this._rangeEncoder, EMatchFinderTypeBT2, posState);
            this._posSlotEncoder[Base.GetLenToPosState(2)].Encode(this._rangeEncoder, 63);
            int posReduced = 1073741824 - 1;
            this._rangeEncoder.EncodeDirectBits(67108863, 26);
            this._posAlignEncoder.ReverseEncode(this._rangeEncoder, 15);
        }
    }

    void Flush(int nowPos) throws IOException {
        ReleaseMFStream();
        WriteEndMarker(this._posStateMask & nowPos);
        this._rangeEncoder.FlushData();
        this._rangeEncoder.FlushStream();
    }

    public void CodeOneBlock(long[] inSize, long[] outSize, boolean[] finished) throws IOException {
        inSize[EMatchFinderTypeBT2] = 0;
        outSize[EMatchFinderTypeBT2] = 0;
        finished[EMatchFinderTypeBT2] = true;
        if (this._inStream != null) {
            this._matchFinder.SetStream(this._inStream);
            this._matchFinder.Init();
            this._needReleaseMFStream = true;
            this._inStream = null;
        }
        if (!this._finished) {
            byte curByte;
            this._finished = true;
            long progressPosValuePrev = this.nowPos64;
            if (this.nowPos64 == 0) {
                if (this._matchFinder.GetNumAvailableBytes() == 0) {
                    Flush((int) this.nowPos64);
                    return;
                }
                ReadMatchDistances();
                this._rangeEncoder.Encode(this._isMatch, (this._state << 4) + (((int) this.nowPos64) & this._posStateMask), EMatchFinderTypeBT2);
                this._state = Base.StateUpdateChar(this._state);
                curByte = this._matchFinder.GetIndexByte(0 - this._additionalOffset);
                this._literalEncoder.GetSubCoder((int) this.nowPos64, this._previousByte).Encode(this._rangeEncoder, curByte);
                this._previousByte = curByte;
                this._additionalOffset--;
                this.nowPos64++;
            }
            if (this._matchFinder.GetNumAvailableBytes() == 0) {
                Flush((int) this.nowPos64);
                return;
            }
            while (true) {
                int len = GetOptimum((int) this.nowPos64);
                int pos = this.backRes;
                int posState = ((int) this.nowPos64) & this._posStateMask;
                int complexState = (this._state << 4) + posState;
                if (len == EMatchFinderTypeBT4 && pos == -1) {
                    this._rangeEncoder.Encode(this._isMatch, complexState, EMatchFinderTypeBT2);
                    curByte = this._matchFinder.GetIndexByte(0 - this._additionalOffset);
                    Encoder2 subCoder = this._literalEncoder.GetSubCoder((int) this.nowPos64, this._previousByte);
                    if (Base.StateIsCharState(this._state)) {
                        subCoder.Encode(this._rangeEncoder, curByte);
                    } else {
                        byte matchByte = this._matchFinder.GetIndexByte(((0 - this._repDistances[EMatchFinderTypeBT2]) - 1) - this._additionalOffset);
                        subCoder.EncodeMatched(this._rangeEncoder, matchByte, curByte);
                    }
                    this._previousByte = curByte;
                    this._state = Base.StateUpdateChar(this._state);
                } else {
                    this._rangeEncoder.Encode(this._isMatch, complexState, EMatchFinderTypeBT4);
                    int distance;
                    int i;
                    if (pos < 4) {
                        this._rangeEncoder.Encode(this._isRep, this._state, EMatchFinderTypeBT4);
                        if (pos == 0) {
                            this._rangeEncoder.Encode(this._isRepG0, this._state, EMatchFinderTypeBT2);
                            if (len == EMatchFinderTypeBT4) {
                                this._rangeEncoder.Encode(this._isRep0Long, complexState, EMatchFinderTypeBT2);
                            } else {
                                this._rangeEncoder.Encode(this._isRep0Long, complexState, EMatchFinderTypeBT4);
                            }
                        } else {
                            this._rangeEncoder.Encode(this._isRepG0, this._state, EMatchFinderTypeBT4);
                            if (pos == EMatchFinderTypeBT4) {
                                this._rangeEncoder.Encode(this._isRepG1, this._state, EMatchFinderTypeBT2);
                            } else {
                                this._rangeEncoder.Encode(this._isRepG1, this._state, EMatchFinderTypeBT4);
                                this._rangeEncoder.Encode(this._isRepG2, this._state, pos - 2);
                            }
                        }
                        if (len == EMatchFinderTypeBT4) {
                            this._state = Base.StateUpdateShortRep(this._state);
                        } else {
                            this._repMatchLenEncoder.Encode(this._rangeEncoder, len - 2, posState);
                            this._state = Base.StateUpdateRep(this._state);
                        }
                        distance = this._repDistances[pos];
                        if (pos != 0) {
                            for (i = pos; i >= EMatchFinderTypeBT4; i--) {
                                this._repDistances[i] = this._repDistances[i - 1];
                            }
                            this._repDistances[EMatchFinderTypeBT2] = distance;
                        }
                    } else {
                        this._rangeEncoder.Encode(this._isRep, this._state, EMatchFinderTypeBT2);
                        this._state = Base.StateUpdateMatch(this._state);
                        this._lenEncoder.Encode(this._rangeEncoder, len - 2, posState);
                        pos -= 4;
                        int posSlot = GetPosSlot(pos);
                        this._posSlotEncoder[Base.GetLenToPosState(len)].Encode(this._rangeEncoder, posSlot);
                        if (posSlot >= 4) {
                            int footerBits = (posSlot >> EMatchFinderTypeBT4) - 1;
                            int baseVal = ((posSlot & EMatchFinderTypeBT4) | 2) << footerBits;
                            int posReduced = pos - baseVal;
                            if (posSlot < 14) {
                                BitTreeEncoder.ReverseEncode(this._posEncoders, (baseVal - posSlot) - 1, this._rangeEncoder, footerBits, posReduced);
                            } else {
                                this._rangeEncoder.EncodeDirectBits(posReduced >> 4, footerBits - 4);
                                this._posAlignEncoder.ReverseEncode(this._rangeEncoder, posReduced & 15);
                                this._alignPriceCount += EMatchFinderTypeBT4;
                            }
                        }
                        distance = pos;
                        for (i = 3; i >= EMatchFinderTypeBT4; i--) {
                            this._repDistances[i] = this._repDistances[i - 1];
                        }
                        this._repDistances[EMatchFinderTypeBT2] = distance;
                        this._matchPriceCount += EMatchFinderTypeBT4;
                    }
                    this._previousByte = this._matchFinder.GetIndexByte((len - 1) - this._additionalOffset);
                }
                this._additionalOffset -= len;
                this.nowPos64 += (long) len;
                if (this._additionalOffset == 0) {
                    if (this._matchPriceCount >= 128) {
                        FillDistancesPrices();
                    }
                    if (this._alignPriceCount >= kNumLenSpecSymbols) {
                        FillAlignPrices();
                    }
                    inSize[EMatchFinderTypeBT2] = this.nowPos64;
                    outSize[EMatchFinderTypeBT2] = this._rangeEncoder.GetProcessedSizeAdd();
                    if (this._matchFinder.GetNumAvailableBytes() == 0) {
                        Flush((int) this.nowPos64);
                        return;
                    } else if (this.nowPos64 - progressPosValuePrev >= 4096) {
                        this._finished = false;
                        finished[EMatchFinderTypeBT2] = false;
                        return;
                    }
                }
            }
        }
    }

    void ReleaseMFStream() {
        if (this._matchFinder != null && this._needReleaseMFStream) {
            this._matchFinder.ReleaseStream();
            this._needReleaseMFStream = false;
        }
    }

    void SetOutStream(OutputStream outStream) {
        this._rangeEncoder.SetStream(outStream);
    }

    void ReleaseOutStream() {
        this._rangeEncoder.ReleaseStream();
    }

    void ReleaseStreams() {
        ReleaseMFStream();
        ReleaseOutStream();
    }

    void SetStreams(InputStream inStream, OutputStream outStream, long inSize, long outSize) {
        this._inStream = inStream;
        this._finished = false;
        Create();
        SetOutStream(outStream);
        Init();
        FillDistancesPrices();
        FillAlignPrices();
        this._lenEncoder.SetTableSize((this._numFastBytes + EMatchFinderTypeBT4) - 2);
        this._lenEncoder.UpdateTables(EMatchFinderTypeBT4 << this._posStateBits);
        this._repMatchLenEncoder.SetTableSize((this._numFastBytes + EMatchFinderTypeBT4) - 2);
        this._repMatchLenEncoder.UpdateTables(EMatchFinderTypeBT4 << this._posStateBits);
        this.nowPos64 = 0;
    }

    public void Code(InputStream inStream, OutputStream outStream, long inSize, long outSize, ICodeProgress progress) throws IOException {
        this._needReleaseMFStream = false;
        try {
            SetStreams(inStream, outStream, inSize, outSize);
            while (true) {
                CodeOneBlock(this.processedInSize, this.processedOutSize, this.finished);
                if (this.finished[EMatchFinderTypeBT2]) {
                    break;
                } else if (progress != null) {
                    progress.SetProgress(this.processedInSize[EMatchFinderTypeBT2], this.processedOutSize[EMatchFinderTypeBT2]);
                }
            }
        } finally {
            ReleaseStreams();
        }
    }

    public void WriteCoderProperties(OutputStream outStream) throws IOException {
        this.properties[EMatchFinderTypeBT2] = (byte) ((((this._posStateBits * kPropSize) + this._numLiteralPosStateBits) * 9) + this._numLiteralContextBits);
        for (int i = EMatchFinderTypeBT2; i < 4; i += EMatchFinderTypeBT4) {
            this.properties[i + EMatchFinderTypeBT4] = (byte) (this._dictionarySize >> (i * 8));
        }
        outStream.write(this.properties, EMatchFinderTypeBT2, kPropSize);
    }

    void FillDistancesPrices() {
        int i;
        for (i = 4; i < Base.kNumFullDistances; i += EMatchFinderTypeBT4) {
            int posSlot = GetPosSlot(i);
            int footerBits = (posSlot >> EMatchFinderTypeBT4) - 1;
            int baseVal = ((posSlot & EMatchFinderTypeBT4) | 2) << footerBits;
            this.tempPrices[i] = BitTreeEncoder.ReverseGetPrice(this._posEncoders, (baseVal - posSlot) - 1, footerBits, i - baseVal);
        }
        for (int lenToPosState = EMatchFinderTypeBT2; lenToPosState < 4; lenToPosState += EMatchFinderTypeBT4) {
            BitTreeEncoder encoder = this._posSlotEncoder[lenToPosState];
            int st = lenToPosState << 6;
            for (posSlot = EMatchFinderTypeBT2; posSlot < this._distTableSize; posSlot += EMatchFinderTypeBT4) {
                this._posSlotPrices[st + posSlot] = encoder.GetPrice(posSlot);
            }
            for (posSlot = 14; posSlot < this._distTableSize; posSlot += EMatchFinderTypeBT4) {
                int[] iArr = this._posSlotPrices;
                int i2 = st + posSlot;
                iArr[i2] = iArr[i2] + ((((posSlot >> EMatchFinderTypeBT4) - 1) - 4) << 6);
            }
            int st2 = lenToPosState * Base.kNumFullDistances;
            i = EMatchFinderTypeBT2;
            while (i < 4) {
                this._distancesPrices[st2 + i] = this._posSlotPrices[st + i];
                i += EMatchFinderTypeBT4;
            }
            while (i < Base.kNumFullDistances) {
                this._distancesPrices[st2 + i] = this._posSlotPrices[GetPosSlot(i) + st] + this.tempPrices[i];
                i += EMatchFinderTypeBT4;
            }
        }
        this._matchPriceCount = EMatchFinderTypeBT2;
    }

    void FillAlignPrices() {
        for (int i = EMatchFinderTypeBT2; i < kNumLenSpecSymbols; i += EMatchFinderTypeBT4) {
            this._alignPrices[i] = this._posAlignEncoder.ReverseGetPrice(i);
        }
        this._alignPriceCount = EMatchFinderTypeBT2;
    }

    public boolean SetAlgorithm(int algorithm) {
        return true;
    }

    public boolean SetDictionarySize(int dictionarySize) {
        if (dictionarySize < EMatchFinderTypeBT4 || dictionarySize > 536870912) {
            return false;
        }
        this._dictionarySize = dictionarySize;
        int dicLogSize = EMatchFinderTypeBT2;
        while (dictionarySize > (EMatchFinderTypeBT4 << dicLogSize)) {
            dicLogSize += EMatchFinderTypeBT4;
        }
        this._distTableSize = dicLogSize * 2;
        return true;
    }

    public boolean SetNumFastBytes(int numFastBytes) {
        if (numFastBytes < kPropSize || numFastBytes > Base.kMatchMaxLen) {
            return false;
        }
        this._numFastBytes = numFastBytes;
        return true;
    }

    public boolean SetMatchFinder(int matchFinderIndex) {
        if (matchFinderIndex < 0 || matchFinderIndex > 2) {
            return false;
        }
        int matchFinderIndexPrev = this._matchFinderType;
        this._matchFinderType = matchFinderIndex;
        if (!(this._matchFinder == null || matchFinderIndexPrev == this._matchFinderType)) {
            this._dictionarySizePrev = -1;
            this._matchFinder = null;
        }
        return true;
    }

    public boolean SetLcLpPb(int lc, int lp, int pb) {
        if (lp < 0 || lp > 4 || lc < 0 || lc > 8 || pb < 0 || pb > 4) {
            return false;
        }
        this._numLiteralPosStateBits = lp;
        this._numLiteralContextBits = lc;
        this._posStateBits = pb;
        this._posStateMask = (EMatchFinderTypeBT4 << this._posStateBits) - 1;
        return true;
    }

    public void SetEndMarkerMode(boolean endMarkerMode) {
        this._writeEndMark = endMarkerMode;
    }
}
