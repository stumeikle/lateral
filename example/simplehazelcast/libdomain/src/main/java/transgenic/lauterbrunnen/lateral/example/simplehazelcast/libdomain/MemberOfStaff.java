package transgenic.lauterbrunnen.lateral.example.simplehazelcast.libdomain;

import java.util.List;

/**
 * Created by stumeikle on 28/05/16.
 */
public class MemberOfStaff extends Member {

    private String niNumber;
    private String jobTitle;
    private String jobDescription;
    private int    payRate;
    private double fractionOfWeekWorked; //50% etc
    private int     jobLevel;
    private List<String> performanceNotes;

}
