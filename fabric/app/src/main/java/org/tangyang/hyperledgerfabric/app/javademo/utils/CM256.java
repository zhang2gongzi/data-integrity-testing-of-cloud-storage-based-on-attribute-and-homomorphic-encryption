package org.tangyang.hyperledgerfabric.app.javademo.utils;


public class CM256 {

    private static int gf_mul(int a, int b) {
        return (a == 0 || b == 0) ? 0 : gfm_log[(gfm_exp[a] + gfm_exp[b]) % 255];
    }

    private static int gf_div(int a, int b) {
        return (a == 0) ? 0 : (b == 0) ? -1 : gfm_log[(gfm_exp[a] + 255 - gfm_exp[b]) % 255];
    }

    private static int[] gfm_exp = new int[512];
    private static int[] gfm_log = new int[256];

    public CM256() {
        initializeGaloisFields();
    }

    private void initializeGaloisFields() {
        int x = 1;
        for (int i = 0; i < 255; i++) {
            gfm_exp[i] = x;
            gfm_log[x] = i;
            x <<= 1;
            if ((x & 0x100) != 0) {
                x ^= 0x11D;
            }
        }
        for (int i = 255; i < 512; i++) {
            gfm_exp[i] = gfm_exp[i - 255];
        }
    }

    private static byte[] gf256mul(byte[] p, int v) {
        byte[] ret = new byte[p.length];
        for (int i = 0; i < p.length; i++) {
            ret[i] = (byte) gfm_exp[(gfm_log[p[i] & 0xFF] + v) % 255];
        }
        return ret;
    }

    private static byte[] gf256add(byte[] p, byte[] q) {
        byte[] ret = new byte[p.length];
        for (int i = 0; i < p.length; i++) {
            ret[i] = (byte) (p[i] ^ q[i]);
        }
        return ret;
    }

    public byte[][] encode(byte[][] data, int numCheckBytes) {
        int m = data.length;
        int k = data[0].length;
        byte[][] encodingMatrix = new byte[m + numCheckBytes][m];
        for (int i = 0; i < m; i++) {
            for (int j = 0; j < m; j++) {
                encodingMatrix[j][i] = (byte) gfm_exp[i * j % 255];
            }
        }
        byte[][] result = new byte[numCheckBytes][k];
        for (int i = 0; i < k; i++) {
            byte[] dataShard = data[i];
            byte[] checksumShard = new byte[numCheckBytes];
            for (int j = 0; j < m; j++) {
                byte[] matrixRow = encodingMatrix[j];
                byte coef = matrixRow[i];
                if (coef != 0) {
                    checksumShard = gf256add(checksumShard, gf256mul(dataShard, coef));
                }
            }
            result[i] = checksumShard;
        }
        return result;
    }

    public byte[][] decode(byte[][] shards, int[] shardIndexes, int numDataShards) {
        int dataShards = shardIndexes.length;
        int[] table = new int[dataShards];
        for (int i = 0; i < dataShards; i++) {
            table[i] = shardIndexes[i];
        }
        byte[][] matrix = new byte[dataShards * 2][dataShards];
        byte[][] datamatrix = new byte[dataShards][dataShards];
        for (int i = 0; i < dataShards; i++) {
            for (int j = 0; j < dataShards; j++) {
                if (i < numDataShards) {
                    if (j < numDataShards) {
                        datamatrix[i][j] = shards[table[i]][j];
                    } else {
                        datamatrix[i][j] = 0;
                        matrix[i][j] = 0;
                        matrix[i + dataShards][j] = 0;
                    }
                } else {
                    if (j < numDataShards) {
                        datamatrix[i][j] = 0;
                        matrix[i][j] = 0;
                        matrix[i + dataShards][j] = 0;
                    } else {
                        matrix[i][j] = (byte) gfm_exp[((i - numDataShards + 1) * (j - numDataShards + 1)) % 255];
                        matrix[i + dataShards][j] = (byte) gfm_exp[((i - numDataShards + 1) * (j - numDataShards + 1)) % 255];
                        datamatrix[i][j] = 0;
                    }
                }
            }
        }
        for (int i = 0; i < numDataShards; i++) {
            if(datamatrix[i][i] == 0){
                int j;
                for(j = i + 1; j < numDataShards; j++){
                    if(datamatrix[j][i] != 0){
                        datamatrix[i] = gf256add(datamatrix[i], datamatrix[j]);
                        matrix[i] = gf256add(matrix[i], matrix[j]);
                    }
                }
                if(datamatrix[i][i] == 0){
                    return null;
                }
                byte coef1 = (byte) gf_div(1, datamatrix[i][i]);
                for(j = 0; j < datamatrix[i].length; j++){
                    datamatrix[i][j] = (byte) (datamatrix[i][j] * coef1 & 0xff);
                }
                for(j = 0; j < matrix[i].length; j++){
                    matrix[i][j] = (byte) (matrix[i][j] * coef1 & 0xff);
                }
            }
            for (int j = 0; j < numDataShards; j++) {
                if (i != j && datamatrix[j][i] != 0) {
                    byte coef = datamatrix[j][i];
                    for (int k = 0; k < datamatrix[i].length; k++) {
                        datamatrix[j][k] = (byte) (datamatrix[j][k] ^ (datamatrix[i][k] * coef & 0xff));
                    }
                    for (int k = 0; k < matrix[i].length; k++) {
                        matrix[j][k] = (byte) (matrix[j][k] ^ (matrix[i][k] * coef & 0xff));
                    }
                }
            }
        }
        for (int i = 0; i < numDataShards; i++) {
            if(datamatrix[i][i] != 1){
                byte coef = (byte) gf_div(1, datamatrix[i][i]);
                for(int j = 0; j < datamatrix[i].length; j++){
                    datamatrix[i][j] = (byte) (datamatrix[i][j] * coef & 0xff);
                }
                for(int j = 0; j < matrix[i].length; j++){
                    matrix[i][j] = (byte) (matrix[i][j] * coef & 0xff);
                }
            }
        }
        byte[][] result = new byte[dataShards][];
        for(int i = 0; i < dataShards; i++){
            result[i] = new byte[shards[table[i]].length];
            for(int j = 0; j < numDataShards; j++){
                result[i] = gf256add(result[i], gf256mul(shards[table[j]], matrix[dataShards + i][j]));
            }
        }
        return result;
    }
}