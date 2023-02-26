# Nuzlocke  [![GitHub](https://img.shields.io/github/license/Pixelmon-Development/API)](https://www.gnu.org/licenses/lgpl-3.0.html)

This project adapts the Nuzlocke Pokémon Challenge for Pixelmon.  Nuzlockes typically operate under 2 main rules: 
1) Only the first Pokémon per route can be caught
2) Any Pokémon that faints is "dead" and must be released or permanently put in the pc

The configuration file for this mod is located at `config/pixelmon/nuzlocke.yml`

The following options can be enabled:

1) require-permission
   - Only begin the nuzlocke once the player has entered "/nuzlocke begin"
     - (true/false)
2) require-nicknames
   - On obtaining a pokemon, immediately open the nickname screen. Blocks names matching species.
     - (true/false)
3) first-encounter-restriction
   - Only the first Pokémon fought or captured for each biome can be caught 
     - (true/false)
4) dupes-clause
   - Encountering a previously caught species does not count as a first encounter
     - (true/false)
5) shiny-clause
   - Shiny Pokémon can always be caught 
     - (true/false)
6) pvp-death-enforced
   - Battles between players cause the faint penalty for any fainted Pokémon 
     - (true/false)
7) faint-result 
   - Result of a pokemon fainting
     - FAINT (Pokemon faints and can be revived using healers or beds)
     - DEAD (Pokemon faints and cannot be revived)
     - DELETE (Pokemon is deleted from the user's party upon battle's end)
8) wipe-result
   - Result of the full team fainting:
     - NONE (No penalty)
     - TELEPORT (Player is teleported to last healer/bed, equivalent to Teleport external move)
     - DEATH (Player is killed, can be used with Hardcore mode to run true Nuzlockes)
9) bag-use
   - Rules for use of items in battle:
     - UNRESTRICTED (All items permitted except for Revival items)
     - NOHEALS (Prevents HP and Status restore items)
     - NOITEMS (Can only use Pokeball section in battle)
10) block-master-ball
    - Prevents the use of Master/Park/Origin balls 
      - (true/false)
11) block-master-ball-in-raids
    - Prevents the use of Master/Park/Origin balls specifically in raids
      - (true/false)
12) block-in-battle-stronger
    - Prevents player from catching overleveled Pokémon in battle
      - (true/false)
13) stronger-threshold
    - Level difference allowed above strongest Pokémon, determines what "overleveled" means
      - integer values
14) block-out-of-battle-legends
    - Prevents player from catching legendary Pokémon outside battle
      - (true/false)
15) block-out-of-battle-mythical
    - Prevents player from catching mythical Pokémon outside battle
        - (true/false)
16) block-out-of-battle-ultra
    - Prevents player from catching ultrabeasts outside battle
        - (true/false)
17) block-out-of-battle-stronger
    - Prevents player from catching overleveled Pokémon outside battle
        - (true/false)
18) elite-trainer-pokemon
    - All trainer Pokémon have max IVs and max EVs in every stat
        - (true/false)
19) trainer-skill
    - Sets trainer battle AI levels
      - STANDARD    (Difficulty matches those set in battle.yml)
      - MIXED       (Randomly picks between aggressive, advanced, and tactical AIs)
      - AGGRESSIVE  (Trainer will use the move that deals the most damage to the opponent, avoiding status moves)
      - TACTICAL    (Trainer knows how to use status moves, but will not switch)
      - ADVANCED    (Trainer knows how to use status moves and will switch Pokémon if advantageous)
20) pokemon-aggression
    - Sets pokemon movement AI
      - TIMID       (All Pokémon attempt to avoid the player)
      - PASSIVE     (All Pokémon are passive)
      - STANDARD    (No Changes, based on other configs)
      - ENCOUNTER   (All Pokémon constantly hunt players until the "first encounter per biome" of the nuzlocke occurs)
      - AGGRESSIVE  (All Pokémon constantly hunt the players, similar to hostile mobs in Vanilla)
