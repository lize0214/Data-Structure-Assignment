package Control;

import Entity.Guest;
import Utility.ControllerResult;
import Utility.ValidationUtility;

public class GuestController extends AbstractEntityController<Guest, String> {

    public GuestController() {
        super("data/guests.txt");
        loadFromFile();
    }

    @Override
    protected Guest parseCsvLine(String line) {
        return Guest.fromCsvLine(line);
    }

    @Override
    protected String toCsvLine(Guest item) {
        return item.toCsvLine();
    }

    @Override
    protected String getKey(Guest item) {
        return item.getGuestId();
    }

    /** Returns the next available system-generated guest ID (for example G009). */
    public String generateNextGuestId() {
        int highestNumber = 0;

        for (int i = 1; i <= list.size(); i++) {
            String guestId = list.getEntry(i).getGuestId();
            if (guestId != null && guestId.matches("G\\d+")) {
                try {
                    int number = Integer.parseInt(guestId.substring(1));
                    if (number > highestNumber) {
                        highestNumber = number;
                    }
                } catch (NumberFormatException ignored) {
                    // Ignore an unusually large legacy ID and continue scanning.
                }
            }
        }

        int nextNumber = highestNumber + 1;
        String candidate;
        do {
            candidate = String.format("G%03d", nextNumber++);
        } while (findByKey(candidate) != null);

        return candidate;
    }

    public ControllerResult addGeneratedGuest(String guestId, String name, String contact) {
        ValidationUtility.ValidationAccumulator acc = new ValidationUtility.ValidationAccumulator();
        acc.check(ValidationUtility.validateRequired(guestId, "Guest ID"));
        acc.check(ValidationUtility.validateRequired(name, "Name"));
        acc.check(ValidationUtility.validateRequired(contact, "Contact"));
        if (acc.hasErrors()) return ControllerResult.fail(acc.getErrorMessage());

        return add(new Guest(guestId, name.trim(), contact.trim()));
    }

    public ControllerResult update(String guestId, String name, String contact) {
        ValidationUtility.ValidationAccumulator acc = new ValidationUtility.ValidationAccumulator();
        acc.check(ValidationUtility.validateRequired(name, "Name"));
        acc.check(ValidationUtility.validateRequired(contact, "Contact"));
        if (acc.hasErrors()) return ControllerResult.fail(acc.getErrorMessage());

        Guest guest = findByKey(guestId);
        if (guest == null) return ControllerResult.fail("Guest not found: " + guestId);

        guest.setGuestId(guestId);
        guest.setName(name);
        guest.setContact(contact);
        saveToFile();
        return ControllerResult.success();
    }

    public ControllerResult updateName(String guestId, String newName) {
        String error = ValidationUtility.validateRequired(newName, "Name");
        if (error != null) return ControllerResult.fail(error);

        Guest guest = findByKey(guestId);
        if (guest == null) return ControllerResult.fail("Guest not found: " + guestId);

        guest.setName(newName);
        saveToFile();
        return ControllerResult.success();
    }

    public ControllerResult updateContact(String guestId, String newContact) {
        String error = ValidationUtility.validateRequired(newContact, "Contact");
        if (error != null) return ControllerResult.fail(error);

        Guest guest = findByKey(guestId);
        if (guest == null) return ControllerResult.fail("Guest not found: " + guestId);

        guest.setContact(newContact);
        saveToFile();
        return ControllerResult.success();
    }
}
