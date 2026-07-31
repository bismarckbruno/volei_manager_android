# Core Usage Scenarios

Scenarios are structured stories describing how users pursue goals with the product.

## Scenario 1: "We need fair teams now"

**Actor:** Manager  
**Context:** Saturday session starts, around 20 players present.  
**Goal:** Start quickly with balanced teams and fewer complaints.  
**Flow:**
1. Manager confirms group settings (team size, streak mode, streak limit).
2. Manager adds/checks participating players.
3. App generates teams with Elo + priority distribution.
4. Group starts match with less subjective intervention.

**Success signal:** Fewer immediate complaints about unfair draw.

## Scenario 2: "A strong team is monopolizing the court"

**Actor:** Manager + recurring players  
**Context:** One team keeps winning repeatedly.  
**Goal:** Preserve competitiveness and keep waiting players engaged.  
**Flow (Rebalance):**
1. Team reaches configured streak limit.
2. App splits winners and recomposes next round.

**Flow (Rest):**
1. Team reaches configured streak limit.
2. If at least two full teams are in queue, winners rest one round.
3. Two waiting teams enter the court.

**Success signal:** Better rotation perception and less "same team forever" frustration.

## Scenario 3: "A player arrives late"

**Actor:** Manager  
**Context:** First match already happened; new player checks in.  
**Goal:** Keep queue fairness without manual arguments.  
**Flow:**
1. Manager adds late player.
2. App applies `dailyToll` based on average matches already played that day.
3. Queue priority remains fair relative to those who arrived earlier.

**Success signal:** Reduced conflict over who should play next.

## Scenario 4: "Avoid exposing players in group chat"

**Actor:** Manager  
**Context:** Group is sensitive about public ranking exposure.  
**Goal:** Keep trust while still using app stats internally.  
**Flow:**
1. Manager uses ranking/history for in-session decisions.
2. Manager avoids broad public sharing of individual-sensitive outputs.
3. Group keeps contributing feedback without feeling judged.

**Success signal:** Continued adoption plus lower social discomfort.
