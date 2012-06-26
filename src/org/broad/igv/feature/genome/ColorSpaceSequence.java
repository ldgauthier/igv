package org.broad.igv.feature.genome;

/**
 * Created with IntelliJ IDEA.
 * User: jrobinso
 * Date: 6/25/12
 * Time: 11:00 AM
 * To change this template use File | Settings | File Templates.
 */
public class ColorSpaceSequence implements Sequence {

    Sequence sequence;

    public ColorSpaceSequence(Sequence sequence) {
        this.sequence = sequence;
    }

    /**
     * Return the sequence in Color Space (SOLID alignment encoding)
     *
     * @param chr
     * @param start
     * @param end
     * @return
     */
    public byte[] getSequence(String chr, int start, int end) {
        // We need to know the base just to the left of the start
        int csStart = (start == 0 ? 0 : start - 1);
        byte[] baseSequence = sequence.getSequence(chr, csStart, end);
        if (baseSequence == null || baseSequence.length == 0) {
            return baseSequence;
        }

        byte[] csSequence = new byte[end - start];
        int i = 0;
        int c1 = start == 0 ? 0 : baseToCS(baseSequence[i++]);
        for (; i < baseSequence.length; i++) {
            int c2 = baseToCS(baseSequence[i]);
            csSequence[i] = (byte) (c1 ^ c2);
        }
        return csSequence;

    }

    private static int baseToCS(byte base) {
        switch (base) {
            case 'A':
            case 'a':
                return 0;
            case 'C':
            case 'c':
                return 1;
            case 'T':
            case 't':
                return 2;
            case 'G':
            case 'g':
                return 3;
        }
        return -1;
    }



    @Override
    public byte getBase(String chr, int position) {
        return 0;  //To change body of implemented methods use File | Settings | File Templates.
    }
}