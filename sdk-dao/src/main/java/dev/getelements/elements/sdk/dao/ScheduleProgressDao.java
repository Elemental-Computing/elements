package dev.getelements.elements.sdk.dao;

import dev.getelements.elements.sdk.model.Pagination;
import dev.getelements.elements.sdk.model.mission.Mission;
import dev.getelements.elements.sdk.model.mission.Progress;
import dev.getelements.elements.sdk.model.mission.Schedule;
import dev.getelements.elements.sdk.model.mission.ScheduleEvent;
import dev.getelements.elements.sdk.model.profile.Profile;
import dev.getelements.elements.sdk.annotation.ElementServiceExport;

import java.util.Collection;
import java.util.List;

/**
 * Manages {@link Progress} instances for the
 */
@ElementServiceExport
public interface ScheduleProgressDao {

    /**
     * Gets all {@link Progress} instances with the supplied profile, schedule, and offet, count
     *
     * @param profileId
     * @param scheduleNameOrId
     * @param offset
     * @param count
     * @return
     */
    Pagination<Progress> getProgresses(String profileId, String scheduleNameOrId, int offset, int count);

    /**
     * Creates {@link Progress} instances for {@link Mission}s in the supplied list. If the missions are already
     * assigned, then no changes will be made. If a {@link Progress} with the mission is already assigned, then
     * this will ensure that the {@link Mission} is now linked to the {@link Schedule} if not already assigned.
     *
     * @param scheduleNameOrId the {@link Schedule} name or ID
     * @param profileId        {@link Profile} identifier
     * @param events
     * @return
     */
    List<Progress> assignProgressesForMissionsIn(String scheduleNameOrId, String profileId, Collection<ScheduleEvent> events);

    /**
     * Deletes {@link Progress} instances for {@link Mission}s not in the supplied list. If no other {@link Schedule}
     * links the {@link Mission}, then this will permamently remove the {@link Progress} instances.
     *
     * @param scheduleNameOrId the {@link Schedule} name or ID
     * @param profileId        {@link Profile} identifier
     * @param events
     * @return
     */
    List<Progress> unassignProgressesForMissionsNotIn(String scheduleNameOrId, String profileId, Collection<ScheduleEvent> events);

}
