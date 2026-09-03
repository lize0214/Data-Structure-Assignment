# VIP Allocation Test Cases (Video Demonstration)

Run the automated tests with `mvn test`.  The VIP test class temporarily loads
its own small dataset and restores the original `data` files after every case.

For the video, enter the VIP Allocation module from the main menu and use the
following cases.  Press `0` whenever the system asks to return.

| ID | Feature | Input / action | Expected validation or result |
|---|---|---|---|
| VIP-01 | Add validation | Add member ID `M999`, room `Suite` | `FAILED: Member not found: M999` |
| VIP-02 | VIP eligibility | Add member ID `M002`, room `Suite` | Gold member is rejected; only Elite, Diamond and Platinum qualify. |
| VIP-03 | Room type validation | Add `M001`, room `Villa` | Invalid room type message and valid choices are shown. |
| VIP-04 | Add VIP | Add `M001`, room `Suite` | Success message; queue size increases by one. |
| VIP-05 | Duplicate prevention | Add `M001` again | `FAILED: Member M001 is already in the VIP queue`. |
| VIP-06 | Priority queue | Add `M003` (Elite), `M008` (Diamond), then view queue | Queue displays Diamond before Elite; Platinum members are above both. |
| VIP-07 | Available rooms | Select **View Available Rooms** | Available, ReadyForCheckIn and Inspected rooms are listed; Occupied/Dirty rooms are not. |
| VIP-08 | Priority allocation | With Platinum, Diamond and Elite waiting, select **Allocate Room** | Highest tier is allocated first; room becomes Occupied and a VIP booking/confirmation number is created. |
| VIP-09 | Preference matching | Add `M001` with `Suite`, then allocate | A Suite is selected when one is allocatable; confirmation and matching details appear. |
| VIP-10 | Preference fallback | Add a VIP requesting a room type with no allocatable room, then allocate | Another allocatable room is assigned successfully rather than losing the queue entry. |
| VIP-11 | Empty allocation | Allocate when queue is empty | `FAILED: No VIP members in the priority queue`. |
| VIP-12 | Cancel/remove | Remove a queued valid ID and confirm `y` | Success message and the member no longer appears in queue. Try a missing ID to show the validation message. |
| VIP-13 | Queue demand report | Reports → Queue & Room Demand Report → Advanced filter/sort | Matching rows, demand-vs-supply shortage, linear search and merge-sort choice are displayed. |
| VIP-14 | Performance report | Reports → Allocation Performance Report | Successful allocations, preference match rate, waiting time and tier/room summaries are displayed. |
| VIP-15 | Report validation | In the performance report, enter a start date later than end date | The UI rejects the range and asks again before generating a report. |

Suggested video order: VIP-01 → VIP-05 (validation), VIP-06 → VIP-10
(normal operation and output), then VIP-12 → VIP-15 (cancellation and reports).
