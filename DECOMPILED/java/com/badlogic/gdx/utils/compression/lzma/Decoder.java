package com.badlogic.gdx.utils.compression.lzma;

import com.badlogic.gdx.Input.Keys;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.graphics.VertexAttributes.Usage;
import com.badlogic.gdx.utils.StreamUtils;
import com.badlogic.gdx.utils.compression.lz.OutWindow;
import com.badlogic.gdx.utils.compression.rangecoder.BitTreeDecoder;
import java.io.IOException;

public class Decoder {
    int m_DictionarySize = -1;
    int m_DictionarySizeCheck = -1;
    short[] m_IsMatchDecoders = new short[192];
    short[] m_IsRep0LongDecoders = new short[192];
    short[] m_IsRepDecoders = new short[12];
    short[] m_IsRepG0Decoders = new short[12];
    short[] m_IsRepG1Decoders = new short[12];
    short[] m_IsRepG2Decoders = new short[12];
    LenDecoder m_LenDecoder = new LenDecoder();
    LiteralDecoder m_LiteralDecoder = new LiteralDecoder();
    OutWindow m_OutWindow = new OutWindow();
    BitTreeDecoder m_PosAlignDecoder = new BitTreeDecoder(4);
    short[] m_PosDecoders = new short[114];
    BitTreeDecoder[] m_PosSlotDecoder = new BitTreeDecoder[4];
    int m_PosStateMask;
    com.badlogic.gdx.utils.compression.rangecoder.Decoder m_RangeDecoder = new com.badlogic.gdx.utils.compression.rangecoder.Decoder();
    LenDecoder m_RepLenDecoder = new LenDecoder();

    class LenDecoder {
        short[] m_Choice = new short[2];
        BitTreeDecoder m_HighCoder = new BitTreeDecoder(8);
        BitTreeDecoder[] m_LowCoder = new BitTreeDecoder[16];
        BitTreeDecoder[] m_MidCoder = new BitTreeDecoder[16];
        int m_NumPosStates = 0;

        LenDecoder() {
        }

        public void Create(int numPosStates) {
            while (this.m_NumPosStates < numPosStates) {
                this.m_LowCoder[this.m_NumPosStates] = new BitTreeDecoder(3);
                this.m_MidCoder[this.m_NumPosStates] = new BitTreeDecoder(3);
                this.m_NumPosStates++;
            }
        }

        public void Init() {
            com.badlogic.gdx.utils.compression.rangecoder.Decoder.InitBitModels(this.m_Choice);
            for (int posState = 0; posState < this.m_NumPosStates; posState++) {
                this.m_LowCoder[posState].Init();
                this.m_MidCoder[posState].Init();
            }
            this.m_HighCoder.Init();
        }

        public int Decode(com.badlogic.gdx.utils.compression.rangecoder.Decoder rangeDecoder, int posState) throws IOException {
            if (rangeDecoder.DecodeBit(this.m_Choice, 0) == 0) {
                return this.m_LowCoder[posState].Decode(rangeDecoder);
            }
            if (rangeDecoder.DecodeBit(this.m_Choice, 1) == 0) {
                return 8 + this.m_MidCoder[posState].Decode(rangeDecoder);
            }
            return 8 + (this.m_HighCoder.Decode(rangeDecoder) + 8);
        }
    }

    class LiteralDecoder {
        Decoder2[] m_Coders;
        int m_NumPosBits;
        int m_NumPrevBits;
        int m_PosMask;

        class Decoder2 {
            short[] m_Decoders = new short[GL20.GL_SRC_COLOR];

            Decoder2() {
            }

            public void Init() {
                com.badlogic.gdx.utils.compression.rangecoder.Decoder.InitBitModels(this.m_Decoders);
            }

            public byte DecodeNormal(com.badlogic.gdx.utils.compression.rangecoder.Decoder rangeDecoder) throws IOException {
                int symbol = 1;
                do {
                    symbol = (symbol << 1) | rangeDecoder.DecodeBit(this.m_Decoders, symbol);
                } while (symbol < Usage.BiNormal);
                return (byte) symbol;
            }

            public byte DecodeWithMatchByte(com.badlogic.gdx.utils.compression.rangecoder.Decoder rangeDecoder, byte matchByte) throws IOException {
                int symbol = 1;
                do {
                    int matchBit = (matchByte >> 7) & 1;
                    matchByte = (byte) (matchByte << 1);
                    int bit = rangeDecoder.DecodeBit(this.m_Decoders, ((matchBit + 1) << 8) + symbol);
                    symbol = (symbol << 1) | bit;
                    if (matchBit != bit) {
                        while (symbol < Usage.BiNormal) {
                            symbol = (symbol << 1) | rangeDecoder.DecodeBit(this.m_Decoders, symbol);
                        }
                    }
                    return (byte) symbol;
                } while (symbol < Usage.BiNormal);
                return (byte) symbol;
            }
        }

        LiteralDecoder() {
        }

        public void Create(int numPosBits, int numPrevBits) {
            if (this.m_Coders == null || this.m_NumPrevBits != numPrevBits || this.m_NumPosBits != numPosBits) {
                this.m_NumPosBits = numPosBits;
                this.m_PosMask = (1 << numPosBits) - 1;
                this.m_NumPrevBits = numPrevBits;
                int numStates = 1 << (this.m_NumPrevBits + this.m_NumPosBits);
                this.m_Coders = new Decoder2[numStates];
                for (int i = 0; i < numStates; i++) {
                    this.m_Coders[i] = new Decoder2();
                }
            }
        }

        public void Init() {
            int numStates = 1 << (this.m_NumPrevBits + this.m_NumPosBits);
            for (int i = 0; i < numStates; i++) {
                this.m_Coders[i].Init();
            }
        }

        Decoder2 GetDecoder(int pos, byte prevByte) {
            return this.m_Coders[((this.m_PosMask & pos) << this.m_NumPrevBits) + ((prevByte & Keys.F12) >>> (8 - this.m_NumPrevBits))];
        }
    }

    public Decoder() {
        for (int i = 0; i < 4; i++) {
            this.m_PosSlotDecoder[i] = new BitTreeDecoder(6);
        }
    }

    boolean SetDictionarySize(int dictionarySize) {
        if (dictionarySize < 0) {
            return false;
        }
        if (this.m_DictionarySize == dictionarySize) {
            return true;
        }
        this.m_DictionarySize = dictionarySize;
        this.m_DictionarySizeCheck = Math.max(this.m_DictionarySize, 1);
        this.m_OutWindow.Create(Math.max(this.m_DictionarySizeCheck, StreamUtils.DEFAULT_BUFFER_SIZE));
        return true;
    }

    boolean SetLcLpPb(int lc, int lp, int pb) {
        if (lc > 8 || lp > 4 || pb > 4) {
            return false;
        }
        this.m_LiteralDecoder.Create(lp, lc);
        int numPosStates = 1 << pb;
        this.m_LenDecoder.Create(numPosStates);
        this.m_RepLenDecoder.Create(numPosStates);
        this.m_PosStateMask = numPosStates - 1;
        return true;
    }

    void Init() throws IOException {
        this.m_OutWindow.Init(false);
        com.badlogic.gdx.utils.compression.rangecoder.Decoder.InitBitModels(this.m_IsMatchDecoders);
        com.badlogic.gdx.utils.compression.rangecoder.Decoder.InitBitModels(this.m_IsRep0LongDecoders);
        com.badlogic.gdx.utils.compression.rangecoder.Decoder.InitBitModels(this.m_IsRepDecoders);
        com.badlogic.gdx.utils.compression.rangecoder.Decoder.InitBitModels(this.m_IsRepG0Decoders);
        com.badlogic.gdx.utils.compression.rangecoder.Decoder.InitBitModels(this.m_IsRepG1Decoders);
        com.badlogic.gdx.utils.compression.rangecoder.Decoder.InitBitModels(this.m_IsRepG2Decoders);
        com.badlogic.gdx.utils.compression.rangecoder.Decoder.InitBitModels(this.m_PosDecoders);
        this.m_LiteralDecoder.Init();
        for (int i = 0; i < 4; i++) {
            this.m_PosSlotDecoder[i].Init();
        }
        this.m_LenDecoder.Init();
        this.m_RepLenDecoder.Init();
        this.m_PosAlignDecoder.Init();
        this.m_RangeDecoder.Init();
    }

    /* JADX WARNING: inconsistent code. */
    /* Code decompiled incorrectly, please refer to instructions dump. */
    public boolean Code(java.io.InputStream r22, java.io.OutputStream r23, long r24) throws java.io.IOException {
        /*
        r21 = this;
        r0 = r21;
        r0 = r0.m_RangeDecoder;
        r18 = r0;
        r0 = r18;
        r1 = r22;
        r0.SetStream(r1);
        r0 = r21;
        r0 = r0.m_OutWindow;
        r18 = r0;
        r0 = r18;
        r1 = r23;
        r0.SetStream(r1);
        r21.Init();
        r17 = com.badlogic.gdx.utils.compression.lzma.Base.StateInit();
        r13 = 0;
        r14 = 0;
        r15 = 0;
        r16 = 0;
        r8 = 0;
        r12 = 0;
    L_0x0029:
        r18 = 0;
        r18 = (r24 > r18 ? 1 : (r24 == r18 ? 0 : -1));
        if (r18 < 0) goto L_0x0033;
    L_0x002f:
        r18 = (r8 > r24 ? 1 : (r8 == r24 ? 0 : -1));
        if (r18 >= 0) goto L_0x01f6;
    L_0x0033:
        r0 = (int) r8;
        r18 = r0;
        r0 = r21;
        r0 = r0.m_PosStateMask;
        r19 = r0;
        r11 = r18 & r19;
        r0 = r21;
        r0 = r0.m_RangeDecoder;
        r18 = r0;
        r0 = r21;
        r0 = r0.m_IsMatchDecoders;
        r19 = r0;
        r20 = r17 << 4;
        r20 = r20 + r11;
        r18 = r18.DecodeBit(r19, r20);
        if (r18 != 0) goto L_0x00a6;
    L_0x0054:
        r0 = r21;
        r0 = r0.m_LiteralDecoder;
        r18 = r0;
        r0 = (int) r8;
        r19 = r0;
        r0 = r18;
        r1 = r19;
        r4 = r0.GetDecoder(r1, r12);
        r18 = com.badlogic.gdx.utils.compression.lzma.Base.StateIsCharState(r17);
        if (r18 != 0) goto L_0x0099;
    L_0x006b:
        r0 = r21;
        r0 = r0.m_RangeDecoder;
        r18 = r0;
        r0 = r21;
        r0 = r0.m_OutWindow;
        r19 = r0;
        r0 = r19;
        r19 = r0.GetByte(r13);
        r0 = r18;
        r1 = r19;
        r12 = r4.DecodeWithMatchByte(r0, r1);
    L_0x0085:
        r0 = r21;
        r0 = r0.m_OutWindow;
        r18 = r0;
        r0 = r18;
        r0.PutByte(r12);
        r17 = com.badlogic.gdx.utils.compression.lzma.Base.StateUpdateChar(r17);
        r18 = 1;
        r8 = r8 + r18;
        goto L_0x0029;
    L_0x0099:
        r0 = r21;
        r0 = r0.m_RangeDecoder;
        r18 = r0;
        r0 = r18;
        r12 = r4.DecodeNormal(r0);
        goto L_0x0085;
    L_0x00a6:
        r0 = r21;
        r0 = r0.m_RangeDecoder;
        r18 = r0;
        r0 = r21;
        r0 = r0.m_IsRepDecoders;
        r19 = r0;
        r0 = r18;
        r1 = r19;
        r2 = r17;
        r18 = r0.DecodeBit(r1, r2);
        r19 = 1;
        r0 = r18;
        r1 = r19;
        if (r0 != r1) goto L_0x0164;
    L_0x00c4:
        r6 = 0;
        r0 = r21;
        r0 = r0.m_RangeDecoder;
        r18 = r0;
        r0 = r21;
        r0 = r0.m_IsRepG0Decoders;
        r19 = r0;
        r0 = r18;
        r1 = r19;
        r2 = r17;
        r18 = r0.DecodeBit(r1, r2);
        if (r18 != 0) goto L_0x0128;
    L_0x00dd:
        r0 = r21;
        r0 = r0.m_RangeDecoder;
        r18 = r0;
        r0 = r21;
        r0 = r0.m_IsRep0LongDecoders;
        r19 = r0;
        r20 = r17 << 4;
        r20 = r20 + r11;
        r18 = r18.DecodeBit(r19, r20);
        if (r18 != 0) goto L_0x00f8;
    L_0x00f3:
        r17 = com.badlogic.gdx.utils.compression.lzma.Base.StateUpdateShortRep(r17);
        r6 = 1;
    L_0x00f8:
        if (r6 != 0) goto L_0x0114;
    L_0x00fa:
        r0 = r21;
        r0 = r0.m_RepLenDecoder;
        r18 = r0;
        r0 = r21;
        r0 = r0.m_RangeDecoder;
        r19 = r0;
        r0 = r18;
        r1 = r19;
        r18 = r0.Decode(r1, r11);
        r6 = r18 + 2;
        r17 = com.badlogic.gdx.utils.compression.lzma.Base.StateUpdateRep(r17);
    L_0x0114:
        r0 = (long) r13;
        r18 = r0;
        r18 = (r18 > r8 ? 1 : (r18 == r8 ? 0 : -1));
        if (r18 >= 0) goto L_0x0125;
    L_0x011b:
        r0 = r21;
        r0 = r0.m_DictionarySizeCheck;
        r18 = r0;
        r0 = r18;
        if (r13 < r0) goto L_0x021c;
    L_0x0125:
        r18 = 0;
    L_0x0127:
        return r18;
    L_0x0128:
        r0 = r21;
        r0 = r0.m_RangeDecoder;
        r18 = r0;
        r0 = r21;
        r0 = r0.m_IsRepG1Decoders;
        r19 = r0;
        r0 = r18;
        r1 = r19;
        r2 = r17;
        r18 = r0.DecodeBit(r1, r2);
        if (r18 != 0) goto L_0x0144;
    L_0x0140:
        r5 = r14;
    L_0x0141:
        r14 = r13;
        r13 = r5;
        goto L_0x00f8;
    L_0x0144:
        r0 = r21;
        r0 = r0.m_RangeDecoder;
        r18 = r0;
        r0 = r21;
        r0 = r0.m_IsRepG2Decoders;
        r19 = r0;
        r0 = r18;
        r1 = r19;
        r2 = r17;
        r18 = r0.DecodeBit(r1, r2);
        if (r18 != 0) goto L_0x015f;
    L_0x015c:
        r5 = r15;
    L_0x015d:
        r15 = r14;
        goto L_0x0141;
    L_0x015f:
        r5 = r16;
        r16 = r15;
        goto L_0x015d;
    L_0x0164:
        r16 = r15;
        r15 = r14;
        r14 = r13;
        r0 = r21;
        r0 = r0.m_LenDecoder;
        r18 = r0;
        r0 = r21;
        r0 = r0.m_RangeDecoder;
        r19 = r0;
        r0 = r18;
        r1 = r19;
        r18 = r0.Decode(r1, r11);
        r6 = r18 + 2;
        r17 = com.badlogic.gdx.utils.compression.lzma.Base.StateUpdateMatch(r17);
        r0 = r21;
        r0 = r0.m_PosSlotDecoder;
        r18 = r0;
        r19 = com.badlogic.gdx.utils.compression.lzma.Base.GetLenToPosState(r6);
        r18 = r18[r19];
        r0 = r21;
        r0 = r0.m_RangeDecoder;
        r19 = r0;
        r10 = r18.Decode(r19);
        r18 = 4;
        r0 = r18;
        if (r10 < r0) goto L_0x0219;
    L_0x019e:
        r18 = r10 >> 1;
        r7 = r18 + -1;
        r18 = r10 & 1;
        r18 = r18 | 2;
        r13 = r18 << r7;
        r18 = 14;
        r0 = r18;
        if (r10 >= r0) goto L_0x01cc;
    L_0x01ae:
        r0 = r21;
        r0 = r0.m_PosDecoders;
        r18 = r0;
        r19 = r13 - r10;
        r19 = r19 + -1;
        r0 = r21;
        r0 = r0.m_RangeDecoder;
        r20 = r0;
        r0 = r18;
        r1 = r19;
        r2 = r20;
        r18 = com.badlogic.gdx.utils.compression.rangecoder.BitTreeDecoder.ReverseDecode(r0, r1, r2, r7);
        r13 = r13 + r18;
        goto L_0x0114;
    L_0x01cc:
        r0 = r21;
        r0 = r0.m_RangeDecoder;
        r18 = r0;
        r19 = r7 + -4;
        r18 = r18.DecodeDirectBits(r19);
        r18 = r18 << 4;
        r13 = r13 + r18;
        r0 = r21;
        r0 = r0.m_PosAlignDecoder;
        r18 = r0;
        r0 = r21;
        r0 = r0.m_RangeDecoder;
        r19 = r0;
        r18 = r18.ReverseDecode(r19);
        r13 = r13 + r18;
        if (r13 >= 0) goto L_0x0114;
    L_0x01f0:
        r18 = -1;
        r0 = r18;
        if (r13 != r0) goto L_0x0215;
    L_0x01f6:
        r0 = r21;
        r0 = r0.m_OutWindow;
        r18 = r0;
        r18.Flush();
        r0 = r21;
        r0 = r0.m_OutWindow;
        r18 = r0;
        r18.ReleaseStream();
        r0 = r21;
        r0 = r0.m_RangeDecoder;
        r18 = r0;
        r18.ReleaseStream();
        r18 = 1;
        goto L_0x0127;
    L_0x0215:
        r18 = 0;
        goto L_0x0127;
    L_0x0219:
        r13 = r10;
        goto L_0x0114;
    L_0x021c:
        r0 = r21;
        r0 = r0.m_OutWindow;
        r18 = r0;
        r0 = r18;
        r0.CopyBlock(r13, r6);
        r0 = (long) r6;
        r18 = r0;
        r8 = r8 + r18;
        r0 = r21;
        r0 = r0.m_OutWindow;
        r18 = r0;
        r19 = 0;
        r12 = r18.GetByte(r19);
        goto L_0x0029;
        */
        throw new UnsupportedOperationException("Method not decompiled: com.badlogic.gdx.utils.compression.lzma.Decoder.Code(java.io.InputStream, java.io.OutputStream, long):boolean");
    }

    public boolean SetDecoderProperties(byte[] properties) {
        if (properties.length < 5) {
            return false;
        }
        int val = properties[0] & Keys.F12;
        int lc = val % 9;
        int remainder = val / 9;
        int lp = remainder % 5;
        int pb = remainder / 5;
        int dictionarySize = 0;
        for (int i = 0; i < 4; i++) {
            dictionarySize += (properties[i + 1] & Keys.F12) << (i * 8);
        }
        if (SetLcLpPb(lc, lp, pb)) {
            return SetDictionarySize(dictionarySize);
        }
        return false;
    }
}
