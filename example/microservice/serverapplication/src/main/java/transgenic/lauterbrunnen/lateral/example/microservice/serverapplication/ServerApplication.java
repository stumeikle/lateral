package transgenic.lauterbrunnen.lateral.example.microservice.serverapplication;

import edu.emory.mathcs.backport.java.util.Arrays;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.log4j.BasicConfigurator;
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
 * Created by stumeikle on 03/11/16.
 *
 * TODO:
 * Ensure unknown types are correctly persisted to the db
 *
 * the rest concept wrt References needs to be rethought. why return whole object graphs on
 * a single retrieve,that makes no sense. should be using references and give the user the option
 * to descend or pull all. i suppose in general smaller bits is better
 *
 * could be clever about it. could say if the object is a reference set the id else set all
 * the fields. that would allow us to defer object structure to the cache layer and prevent us
 * duplicating the logic
 */
public class ServerApplication {

    private static final Log LOG = LogFactory.getLog(ServerApplication.class);

    public static void main(String[] args) {

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
        }catch(PersistenceException pe) {
            pe.printStackTrace();
        } catch (MalformedURLException e) {
            e.printStackTrace();
        }
    }
}
