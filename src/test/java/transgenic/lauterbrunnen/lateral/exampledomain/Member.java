package transgenic.lauterbrunnen.lateral.exampledomain;


import transgenic.lauterbrunnen.lateral.domain.RepositoryId;
import transgenic.lauterbrunnen.lateral.domain.UniqueId;

/**
 * Created by stumeikle on 28/05/16.
 */
public class Member {

    @RepositoryId
    private UniqueId id;
    private String forename; //duplicated in contact details
    private String surname;  //duplicated in contact details
    private long dob;
    private ContactDetails contactDetails;
    private ContactDetails emergencyContact;
}
