package org.broad.igv.feature.tribble;

import org.broad.igv.feature.genome.Genome;
import org.broad.igv.track.Track;
import org.broad.igv.track.TrackLoader;
import org.broad.igv.util.ResourceLocator;
import org.broad.igv.util.TestUtils;
import org.broad.tribble.AbstractFeatureReader;
import org.broad.tribble.Feature;
import org.broad.tribble.FeatureCodec;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import java.util.List;

import static junit.framework.Assert.assertEquals;

/**
 * User: jacob
 * Date: 2012/03/08
 */
public class UCSCGeneTableCodecTest {

    @Before
    public void setUp() throws Exception {
        TestUtils.setUpHeadless();
    }

    @After
    public void tearDown() throws Exception {

    }

    @Test
    public void testLoadGenePred() throws Exception {
        String file = TestUtils.DATA_DIR + "/gene/EnsembleGenes_sample.genepred";
        TrackLoader loader = new TrackLoader();
        ResourceLocator locator = new ResourceLocator(file);
        Genome genome = TestUtils.loadGenome();
        List<Track> tracks = loader.load(locator, genome);

        assertEquals(1, tracks.size());

        FeatureCodec codec = CodecFactory.getCodec(locator.getPath(), genome);
        AbstractFeatureReader<Feature> bfs = AbstractFeatureReader.getFeatureReader(locator.getPath(), codec, false);
        Iterable<Feature> iter = bfs.iterator();
        int count = 0;
        for (Feature f : iter) {
            if (count == 0) {
                assertEquals(67051161, f.getStart());
                assertEquals(67163158, f.getEnd());
            }
            count++;
        }

        assertEquals(74, count);

    }

    @Test
    public void testLoadUCSC() throws Exception {
        String file = TestUtils.DATA_DIR + "/gene/UCSCgenes_sample.gene";
        TrackLoader loader = new TrackLoader();
        ResourceLocator locator = new ResourceLocator(file);
        Genome genome = TestUtils.loadGenome();
        List<Track> tracks = loader.load(locator, genome);

        assertEquals(1, tracks.size());

        FeatureCodec codec = CodecFactory.getCodec(locator.getPath(), genome);
        AbstractFeatureReader<Feature> bfs = AbstractFeatureReader.getFeatureReader(locator.getPath(), codec, false);
        Iterable<Feature> iter = bfs.iterator();
        int count = 0;
        for (Feature f : iter) {
            if (count == 0) {
                assertEquals(1115, f.getStart());
                assertEquals(4121, f.getEnd());
            }
            count++;
        }

        assertEquals(382, count);

    }


}