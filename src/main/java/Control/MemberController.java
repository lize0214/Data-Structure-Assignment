package Control;

import Entity.Member;
import Utility.ControllerResult;
import Utility.ValidationUtility;

public class MemberController extends AbstractEntityController<Member, String> {

    public MemberController() {
        super("data/members.txt");
        loadFromFile();
    }

    @Override
    protected Member parseCsvLine(String line) {
        return Member.fromCsvLine(line);
    }

    @Override
    protected String toCsvLine(Member item) {
        return item.toCsvLine();
    }

    @Override
    protected String getKey(Member item) {
        return item.getMemberId();
    }

    public ControllerResult update(String memberId, String name, String tier, int points) {
        ValidationUtility.ValidationAccumulator acc = new ValidationUtility.ValidationAccumulator();
        acc.check(ValidationUtility.validateRequired(name, "Name"));
        acc.check(ValidationUtility.validateMemberTier(tier));
        acc.check(ValidationUtility.validateNonNegative(points, "Points"));
        if (acc.hasErrors()) return ControllerResult.fail(acc.getErrorMessage());

        Member member = findByKey(memberId);
        if (member == null) return ControllerResult.fail("Member not found: " + memberId);

        member.setMemberId(memberId);
        member.setName(name);
        member.setTier(tier);
        member.setPoints(points);
        saveToFile();
        return ControllerResult.success();
    }

    public ControllerResult updateName(String memberId, String newName) {
        String error = ValidationUtility.validateRequired(newName, "Name");
        if (error != null) return ControllerResult.fail(error);

        Member member = findByKey(memberId);
        if (member == null) return ControllerResult.fail("Member not found: " + memberId);

        member.setName(newName);
        saveToFile();
        return ControllerResult.success();
    }

    public ControllerResult updateTier(String memberId, String newTier) {
        String error = ValidationUtility.validateMemberTier(newTier);
        if (error != null) return ControllerResult.fail(error);

        Member member = findByKey(memberId);
        if (member == null) return ControllerResult.fail("Member not found: " + memberId);

        member.setTier(newTier);
        saveToFile();
        return ControllerResult.success();
    }

    public ControllerResult updatePoints(String memberId, int newPoints) {
        String error = ValidationUtility.validateNonNegative(newPoints, "Points");
        if (error != null) return ControllerResult.fail(error);

        Member member = findByKey(memberId);
        if (member == null) return ControllerResult.fail("Member not found: " + memberId);

        member.setPoints(newPoints);
        saveToFile();
        return ControllerResult.success();
    }
}
