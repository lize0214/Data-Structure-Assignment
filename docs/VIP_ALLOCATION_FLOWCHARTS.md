# VIP Allocation Module Flowcharts

The following flowcharts show only the main processes of each function in the VIP Allocation Module.

## 1. VIP Allocation Main Menu

```mermaid
flowchart TD
    A([Enter VIP Allocation Module]) --> B[Display VIP Menu]
    B --> C{Select a Function}
    C -->|1| D[Add VIP to Queue]
    C -->|2| E[Allocate Room]
    C -->|3| F[View VIP Queue]
    C -->|4| G[Remove VIP]
    C -->|5| H[View Available Rooms]
    C -->|6| I[View Reports]
    C -->|0| J([Return to Main Menu])
    D --> B
    E --> B
    F --> B
    G --> B
    H --> B
    I --> B
```

## 2. Add VIP to Priority Queue

```mermaid
flowchart TD
    A([Start]) --> B[/Enter Member ID and Preferred Room Type/]
    B --> C{Is the Member Information Valid?}
    C -->|No| D[Display Error]
    C -->|Yes| E{Eligible VIP Tier and Not Already in Queue?}
    E -->|No| D
    E -->|Yes| F[Set Priority Based on Tier]
    F --> G[Add Member to VIP Priority Queue]
    G --> H[Save Queue]
    H --> I[Display Success Message]
    D --> J([End])
    I --> J
```

Priority order: `Platinum > Diamond > Elite`. Members in the same tier are served according to their registration time.

## 3. Allocate Room to the Next VIP

```mermaid
flowchart TD
    A([Start]) --> B{Is There a Member in the VIP Queue?}
    B -->|No| C[Display No VIP Waiting]
    B -->|Yes| D{Is a Room Available?}
    D -->|No| E[Display No Available Room]
    D -->|Yes| F[Get the Highest-Priority VIP]
    F --> G[Search for Preferred Room Type First]
    G --> H[Allocate Room and Create Booking]
    H --> I{Was the Allocation Successful?}
    I -->|No| J[Restore Original Data and Display Error]
    I -->|Yes| K[Update Room, Queue, and Allocation History]
    K --> L[Display Allocation Result]
    C --> M([End])
    E --> M
    J --> M
    L --> M
```

## 4. View VIP Priority Queue

```mermaid
flowchart TD
    A([Start]) --> B[Retrieve VIP Queue]
    B --> C{Is the Queue Empty?}
    C -->|Yes| D[Display Queue Empty]
    C -->|No| E[Display VIPs by Priority]
    E --> F[Show Member, Tier, Room Preference, and Registration Time]
    D --> G([End])
    F --> G
```

## 5. Remove VIP Member

```mermaid
flowchart TD
    A([Start]) --> B[/Enter Member ID/]
    B --> C{Confirm Removal?}
    C -->|No| D[Cancel Operation]
    C -->|Yes| E{Is the Member in the VIP Queue?}
    E -->|No| F[Display Error]
    E -->|Yes| G[Remove Member from Queue]
    G --> H[Save Queue]
    H --> I[Display Success Message]
    D --> J([End])
    F --> J
    I --> J
```

## 6. View Available Rooms

```mermaid
flowchart TD
    A([Start]) --> B[Retrieve All Rooms]
    B --> C[Filter Rooms with Allocatable Status]
    C --> D{Are Any Rooms Available?}
    D -->|No| E[Display No Available Rooms]
    D -->|Yes| F[Display Room Number, Type, Price, and Status]
    E --> G([End])
    F --> G
```

## 7. VIP Queue and Room Demand Report

```mermaid
flowchart TD
    A([Start]) --> B[Retrieve Current VIP Queue and Available Rooms]
    B --> C[Apply Search, Filter, and Sorting Criteria]
    C --> D[Calculate Waiting Members by Tier]
    D --> E[Compare Room Demand with Supply]
    E --> F[Display Queue, Waiting Time, and Room Shortage]
    F --> G{Select Next Action}
    G -->|Advanced Search| C
    G -->|Reset| B
    G -->|Return| H([Return to Reports Menu])
```

## 8. VIP Allocation Performance Report

```mermaid
flowchart TD
    A([Start]) --> B[Retrieve VIP Allocation History]
    B --> C[Apply Date, Search, Filter, and Sorting Criteria]
    C --> D[Calculate Total Allocations and Waiting Time]
    D --> E[Calculate Room Preference Match Rate]
    E --> F[Display Allocation Records and Summary]
    F --> G{Select Next Action}
    G -->|Advanced Search| C
    G -->|Reset| B
    G -->|Return| H([Return to Reports Menu])
```
