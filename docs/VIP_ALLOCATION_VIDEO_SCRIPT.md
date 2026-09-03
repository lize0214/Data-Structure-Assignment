# VIP Allocation Module — Video Script

Replace the text inside square brackets before recording.  This script follows
the lecturer's required order and can be completed in about 6–8 minutes.

## A. Introduction (0:00–0:35)

Say:

> Good [morning/afternoon], we are Group [NUMBER].  Our members are [NAME 1],
> [NAME 2], [NAME 3] and [NAME 4].  Our system is **TARUMT Resorts System**.
> In this video, we demonstrate the VIP Priority Room Allocation module.  It
> manages VIP queue registration, priority room allocation, cancellation,
> available-room viewing and management reports.  The selected ADT is a
> **max-heap priority queue**.  This system does not use a database; it stores
> persistent records in text files under the `data` folder.

Show the project title/main menu, then open the `data` folder briefly.

## B. System Structure and Code Overview (0:35–2:20)

Show the package tree, then say:

> The project is separated into `ADT`, `Boundary`, `Control`, `Entity` and
> `Utility` packages.  For this module, `Boundary.VIPAllocationUI` handles the
> menu and user input, `Control.VIPAllocationController` contains the business
> rules, and the `Entity` package stores queue and report records.

Open `ADT/PriorityQueueInterface.java` and say:

> This is the ADT specification.  It defines priority-queue operations such as
> `enqueue`, `dequeue`, `peek`, `isEmpty` and `size`, without deciding how the
> data is stored.

Open `ADT/HeapPriorityQueue.java` and say:

> This is the implementation.  It uses our custom max heap, so the entry with
> the highest priority is removed first.

Open `Entity/VIPQueueEntry.java`, show `compareTo`, and say:

> Priority is determined first by membership tier: Platinum, Diamond, then
> Elite.  For members in the same tier, the earlier registration time wins,
> which gives FIFO behaviour within that tier.

Open `Control/VIPAllocationController.java` and show the declaration near the
top:

> The ADT is declared here as `HeapPriorityQueue<VIPQueueEntry> vipQueue`.

Then show these three focused methods:

1. `enqueueVIPMember` — validates member ID, VIP tier, room type and duplicate
   entries before calling `vipQueue.enqueue(entry)`.
2. `allocateNextVIPRoom` — calls `vipQueue.dequeue()` to select the highest
   priority member, finds a suitable room, updates its status and creates a VIP
   booking.
3. `viewQueue` — dequeues entries into a temporary list and re-enqueues them,
   allowing the queue to be shown in priority order without changing it.

Keep the code section short: point to these lines/functions; do not read the
whole source code.

## C. System Demonstration (2:20–end)

Use the VIP Allocation menu.  After every operation, enter `0` when prompted
to return.

### 1. Validation (2:20–3:10)

1. Select **1 Add VIP Member**. Enter `M999`, then `Suite`.
   - Say: “The system rejects a member ID that does not exist.”
2. Select **1 Add VIP Member**. Enter `M002`, then `Suite`.
   - Say: “M002 is Gold, therefore not eligible. Only Elite, Diamond and
     Platinum members can enter the VIP allocation queue.”
3. Select **1 Add VIP Member**. Enter `M001`, then `Villa`.
   - Say: “The preferred room type is also validated.”

### 2. Priority Queue and Allocation (3:10–4:35)

1. Add `M003` with room preference `Deluxe` (Elite).
2. Add `M008` with room preference `Suite` (Diamond).
3. Add `M001` with room preference `Suite` (Platinum).
4. Select **3 View VIP Priority Queue**.
   - Say: “Although the members were entered in another order, the queue is
     sorted by priority: Platinum first, Diamond second and Elite third.”
5. Add `M001` again to show the duplicate-entry validation.
6. Select **5 View Available Rooms**.
   - Say: “Only Available, ReadyForCheckIn and Inspected rooms are allocatable.”
7. Select **2 Allocate Room to Next VIP Member**.
   - Say: “The Platinum member is selected using `dequeue`.  The system
     allocates a preferred Suite when it is available, updates its room status
     to Occupied and generates a confirmation number.”
8. Select **3 View VIP Priority Queue** again to show that M001 was removed
   while M008 and M003 remain.

### 3. Cancellation and Reports (4:35–6:00)

1. Select **4 Remove VIP Member**. Enter `M003`, confirm with `y`.
   - Say: “The cancellation removes only the chosen member and rebuilds the
     heap while preserving other priorities.”
2. Select **6 View Reports → 1 VIP Queue & Room Demand Report**.
   - Show the queue, demand versus allocatable supply, shortage and summary.
   - Select Advanced Search/Filter/Sort and choose a tier or preferred-room
     filter.  Say: “The report uses a linear search to filter and merge sort to
     order the displayed records.”
3. Return, then select **2 VIP Allocation Performance Report**.
   - Say: “This report shows successful allocation history, waiting time and
     preference match rate.”
4. In advanced options, enter a start date later than the end date (for
   example, `2026-08-26` then `2026-08-25`).
   - Say: “The UI prevents an invalid date range before generating the report.”

## D. Closing (last 10 seconds)

Say:

> In summary, the VIP module uses a custom max-heap priority queue to enforce
> membership priority and FIFO ordering within the same tier.  It validates
> inputs, updates room and booking records, and provides management reports.

## Optional: Automated Test Evidence

Show `src/test/java/Control/VIPAllocationControllerTest.java`, then run:

```powershell
mvn -s maven-settings.xml test
```

The test suite covers all public VIP allocation functions and its validation
cases.  It uses a temporary fixture and restores the original data files after
each test.
