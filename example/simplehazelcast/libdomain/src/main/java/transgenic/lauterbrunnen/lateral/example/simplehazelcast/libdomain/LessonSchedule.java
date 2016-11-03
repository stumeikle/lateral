package transgenic.lauterbrunnen.lateral.example.simplehazelcast.libdomain;


import transgenic.lauterbrunnen.lateral.domain.RepositoryId;

import java.util.List;

/**
 * Created by stumeikle on 28/05/16.
 */
public class LessonSchedule{

    @RepositoryId
    private Integer id;
    private List<SubjectDayTime> subjectDayTimeList;
}
