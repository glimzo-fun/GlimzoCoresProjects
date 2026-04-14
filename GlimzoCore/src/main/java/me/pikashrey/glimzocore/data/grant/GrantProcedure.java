package me.pikashrey.glimzocore.data.grant;

import me.pikashrey.glimzocore.api.rank.Rank;
import me.pikashrey.glimzocore.api.rank.RankRef;

import java.util.UUID;

public class GrantProcedure {

    public static final long TIMEOUT_MS = 5 * 60 * 1000L; // 5 minutes

    private final UUID               staffUuid;
    private final String             staffName;
    private final UUID               targetUuid;
    private final String             targetName;
    private       RankRef            selectedRank;
    private       long               selectedDuration; // ms; -1 = permanent
    private       GrantProcedureState state;
    private final long               startedAt;

    public GrantProcedure(UUID staffUuid, String staffName, UUID targetUuid, String targetName) {
        this.staffUuid        = staffUuid;
        this.staffName        = staffName;
        this.targetUuid       = targetUuid;
        this.targetName       = targetName;
        this.state            = GrantProcedureState.SELECT_RANK;
        this.selectedDuration = -1;
        this.startedAt        = System.currentTimeMillis();
    }

    public boolean isExpired() {
        return System.currentTimeMillis() - startedAt > TIMEOUT_MS;
    }

    public boolean isComplete() {
        return state == GrantProcedureState.CONFIRM && selectedRank != null;
    }

    /** Computed expiry epoch ms for the resulting grant. -1 if permanent. */
    public long computeExpiresAt() {
        if (selectedDuration == -1) return -1;
        return System.currentTimeMillis() + selectedDuration;
    }

    public UUID               getStaffUuid()       { return staffUuid; }
    public String             getStaffName()        { return staffName; }
    public UUID               getTargetUuid()       { return targetUuid; }
    public String             getTargetName()       { return targetName; }
    public RankRef            getSelectedRank()     { return selectedRank; }
    public void               setSelectedRank(RankRef r) { this.selectedRank = r; }

    /** Legacy setter accepting enum Rank for callers that haven't been migrated. */
    public void               setSelectedRank(Rank r) { this.selectedRank = RankRef.of(r); }

    public long               getSelectedDuration() { return selectedDuration; }
    public void               setSelectedDuration(long d) { this.selectedDuration = d; }
    public GrantProcedureState getState()           { return state; }
    public void                setState(GrantProcedureState s) { this.state = s; }
    public long               getStartedAt()        { return startedAt; }
}
