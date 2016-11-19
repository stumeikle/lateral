package transgenic.lauterbrunnen.lateral.example.microservice.serverapplication;

import edu.emory.mathcs.backport.java.util.Arrays;
import org.apache.log4j.BasicConfigurator;
import org.junit.Test;
import transgenic.lauterbrunnen.lateral.Lateral;
import transgenic.lauterbrunnen.lateral.domain.Factory;
import transgenic.lauterbrunnen.lateral.domain.PersistenceException;
import transgenic.lauterbrunnen.lateral.domain.Repository;
import transgenic.lauterbrunnen.lateral.example.microservice.libdomain.generated.Album;
import transgenic.lauterbrunnen.lateral.example.microservice.libdomain.generated.Artist;
import transgenic.lauterbrunnen.lateral.example.microservice.libdomain.generated.Track;

import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;

/**
 * Created by stumeikle on 17/11/16.
 */
public class TestPersist {

    @Test
    public void test1() {
        BasicConfigurator.configure();

        //(1) start the server
        //    .. done by the plugins

        Lateral.INSTANCE.initialise();

        System.out.println("Persisting album");
        try {
            Album a = Factory.create(Album.class);
            Artist pinkFloyd = Factory.create(Artist.class);
            pinkFloyd.setName("pink floyd");
            List<Album> albumList = new ArrayList<>();
            albumList.add(a);
            pinkFloyd.setAlbumList(albumList);

            Track track1 = Factory.create(Track.class);
            track1.setAlbum(a);
            track1.setArtist(pinkFloyd);
            track1.setName("the wall");

            a.setArtist(pinkFloyd);
            a.setCoverArt(new URL("http://https://upload.wikimedia.org/wikipedia/en/archive/1/13/20160920225603!PinkFloydWallCoverOriginalNoText.jpg"));
            a.setTrackList(Arrays.asList(new Track[]{track1}));
            a.setName("the wall");

            Repository.persist(a);
        } catch (PersistenceException pe) {
            pe.printStackTrace();
        } catch (MalformedURLException e) {
            e.printStackTrace();
        }
    }
}
