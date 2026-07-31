# Product Vision and Personas

## Product Positioning

- **App Name:** Volley Manager
- **Primary Audience:** The organizer of a recreational volleyball group (the "Manager")
- **Value Proposition:** Automate match operations, reduce social friction caused by "cliques", and keep court time fair through transparent rules.

## Persona 1: The Manager (Primary Persona)

### Profile
- Usually 20-35 years old
- Organizes matches mostly on Saturdays (sometimes holidays)
- Runs sessions with around 20 players
- Uses the app as the operational controller during game day

### Core Motivations
- Start the day with logistics already under control
- Avoid favoritism discussions and manual negotiations
- Keep the group engaged because games feel fair and dynamic
- Save mental effort compared to paper notes or ad-hoc decisions

### Main Pains
- Team cliques and perceived unfair squads
- Strong players concentrating in one team
- Repeated arguments about who should play next
- Discomfort when performance visibility feels too exposing

### What Volley Manager Solves
- **Automatic balancing** using Elo + `isPriority` distribution
- **Streak rules** (Rebalance/Rest) to prevent court monopoly
- **Fair queue management** based on who played less
- **Late-arrival toll (`dailyToll`)** to avoid queue-cutting perception
- **Config-first onboarding** to define rules before starting matches

## Persona 2: The Recurring Player (Secondary Persona)

### Profile
- Usually 15-40 years old, with concentration between 20-30
- Attends weekly or frequent recreational sessions
- May not operate the app directly, but is strongly affected by it

### Core Motivations
- Play often enough during the event
- Feel that team composition is balanced
- Avoid social tension and subjective decisions

### Sensitivities
- Fairness matters more than strict formal competition
- Public ranking sharing can be perceived as exposure by part of the group

## Highest-Value Problem Being Solved

The app primarily solves **perceived unfairness in court access and team composition**, which is a social problem before it is a technical one. By turning decisions into explicit, configurable, and repeatable rules, Volley Manager reduces conflict and increases trust in the session flow.
