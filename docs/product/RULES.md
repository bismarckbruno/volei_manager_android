# Business Rules and Algorithms

This document maps product rules to user pains and expected outcomes.

## Rule Matrix

| Feature / Rule | Current Behavior | User Pain Addressed |
| --- | --- | --- |
| **Automatic team balancing** | Builds teams trying to keep competitive parity using Elo averages and priority distribution. | "Clique" teams and repeated mismatch complaints. |
| **Priority flag (`isPriority`)** | Guarantees at least one priority player per team (when available). | Unbalanced role/attribute distribution (setters, gender balance, etc.). |
| **Late-arrival toll (`dailyToll`)** | If a player arrives after the first match, a daily toll is applied as the average number of matches already played that day. | Perception that late arrivals "cut the line" for court time. |
| **Victory streak limit** | Configurable integer from 1 to 6 (default often 3; 2 and 3 commonly used). | Same team dominating the court for too long. |
| **Streak handling: Rebalance mode** | When streak limit is hit, winning team is split for the next round. | Long winning runs with little team turnover. |
| **Streak handling: Rest mode** | When streak limit is hit and there are at least two full teams in queue, winners can rest one round and waiting teams enter. | Queue frustration when many players are waiting. |
| **Queue fairness** | Entry/stay logic favors players with fewer matches played. | "I barely played today" complaints. |
| **Scoreboard (optional usage)** | Match score can be tracked in-app but is not mandatory. | Groups that want flexible formality (casual vs. tracked sessions). |
| **Onboarding flow for new groups** | Configuration is emphasized before starting game flow. | Early-session misconfiguration and rushed starts. |

## Privacy and Social Safety Notes

- Ranking/stat sharing should be treated as opt-in social behavior.
- Group comfort may vary; avoid broadcasting individual performance without explicit agreement.
- Product communication should position Elo as a balancing aid, not as public judgment.

## Product Constraints (Current Scope)

- No external scoreboard integration yet.
- Focus is recreational session management, not official tournament operations.
- Team and queue behavior are group-configurable but still centered on fairness and flow.
