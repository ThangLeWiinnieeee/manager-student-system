
package vn.edu.fit.topicmanagement.studentgroup;

public class MyGroupView {

    private final Long id;
    private final String name;
    private final Long roundId;
    private final boolean leader;

    public MyGroupView(
            Long id,
            String name,
            Long roundId,
            boolean leader) {

        this.id = id;
        this.name = name;
        this.roundId = roundId;
        this.leader = leader;
    }

    public Long getId() {
        return id;
    }

    public String getName() {
        return name;
    }

    public Long getRoundId() {
        return roundId;
    }

    public boolean isLeader() {
        return leader;
    }
}
