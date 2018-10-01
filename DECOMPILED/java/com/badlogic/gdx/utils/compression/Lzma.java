package com.badlogic.gdx.utils.compression;

import com.badlogic.gdx.Input.Keys;
import com.badlogic.gdx.utils.compression.lzma.Base;
import com.badlogic.gdx.utils.compression.lzma.Decoder;
import com.badlogic.gdx.utils.compression.lzma.Encoder;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

public class Lzma {

    static class CommandLine {
        public static final int kBenchmak = 2;
        public static final int kDecode = 1;
        public static final int kEncode = 0;
        public int Algorithm = kBenchmak;
        public int Command = -1;
        public int DictionarySize = 8388608;
        public boolean DictionarySizeIsDefined = false;
        public boolean Eos = false;
        public int Fb = Base.kNumFullDistances;
        public boolean FbIsDefined = false;
        public String InFile;
        public int Lc = 3;
        public int Lp = 0;
        public int MatchFinder = kDecode;
        public int NumBenchmarkPasses = 10;
        public String OutFile;
        public int Pb = kBenchmak;

        CommandLine() {
        }
    }

    public static void compress(InputStream in, OutputStream out) throws IOException {
        CommandLine params = new CommandLine();
        boolean eos = false;
        if (params.Eos) {
            eos = true;
        }
        Encoder encoder = new Encoder();
        if (!encoder.SetAlgorithm(params.Algorithm)) {
            throw new RuntimeException("Incorrect compression mode");
        } else if (!encoder.SetDictionarySize(params.DictionarySize)) {
            throw new RuntimeException("Incorrect dictionary size");
        } else if (!encoder.SetNumFastBytes(params.Fb)) {
            throw new RuntimeException("Incorrect -fb value");
        } else if (!encoder.SetMatchFinder(params.MatchFinder)) {
            throw new RuntimeException("Incorrect -mf value");
        } else if (encoder.SetLcLpPb(params.Lc, params.Lp, params.Pb)) {
            long fileSize;
            encoder.SetEndMarkerMode(eos);
            encoder.WriteCoderProperties(out);
            if (eos) {
                fileSize = -1;
            } else {
                fileSize = (long) in.available();
                if (fileSize == 0) {
                    fileSize = -1;
                }
            }
            for (int i = 0; i < 8; i++) {
                out.write(((int) (fileSize >>> (i * 8))) & Keys.F12);
            }
            encoder.Code(in, out, -1, -1, null);
        } else {
            throw new RuntimeException("Incorrect -lc or -lp or -pb value");
        }
    }

    public static void decompress(InputStream in, OutputStream out) throws IOException {
        byte[] properties = new byte[5];
        if (in.read(properties, 0, 5) != 5) {
            throw new RuntimeException("input .lzma file is too short");
        }
        Decoder decoder = new Decoder();
        if (decoder.SetDecoderProperties(properties)) {
            long outSize = 0;
            for (int i = 0; i < 8; i++) {
                int v = in.read();
                if (v < 0) {
                    throw new RuntimeException("Can't read stream size");
                }
                outSize |= ((long) v) << (i * 8);
            }
            if (!decoder.Code(in, out, outSize)) {
                throw new RuntimeException("Error in data stream");
            }
            return;
        }
        throw new RuntimeException("Incorrect stream properties");
    }
}
