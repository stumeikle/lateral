package transgenic.lauterbrunnen.lateral.example.simplehazelcast.serverdbdumper;

import transgenic.lauterbrunnen.lateral.example.simplehazelcast.serverdbdumper.persist.hazelcast.generated.*;

/**
 * Created by stumeikle on 01/12/16.
 */
public class EntityAdminEndpointManager {

    public EntityAdminEndpointManager() {
        new AddressAdminEndpoint();
        new ContactDetailsAdminEndpoint();
        new DeputyHeadAdminEndpoint();
        new HeadAdminEndpoint();
        new HeadOfHouseAdminEndpoint();
        new HouseAdminEndpoint();
        new JanitorAdminEndpoint();
        new LessonScheduleAdminEndpoint();
        new MemberAdminEndpoint();
        new MemberOfStaffAdminEndpoint();
        new PupilAdminEndpoint();
        new RoomAdminEndpoint();
        new SchoolAdminEndpoint();
        new SchoolClassAdminEndpoint();
        new SubjectDayTimeAdminEndpoint();
        new SupplyTeacherAdminEndpoint();
        new TeacherAdminEndpoint();
    }
}
