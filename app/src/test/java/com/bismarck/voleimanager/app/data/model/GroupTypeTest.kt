package com.bismarck.voleimanager.app.data.model

import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class GroupTypeTest {

    @Test
    fun teamSizeRangesMatchEachMode() {
        assertEquals(2..6, GroupType.RECREATIONAL.teamSizeRange)
        assertEquals(2..7, GroupType.FIXED_POSITIONS.teamSizeRange)
        assertEquals(2..14, GroupType.TOURNAMENT_RECREATIONAL.teamSizeRange)
        assertEquals(2..14, GroupType.TOURNAMENT_PRO.teamSizeRange)
    }

    @Test
    fun onlyModesWithoutPositionsSupportPriority() {
        assertTrue(GroupType.RECREATIONAL.supportsPriority)
        assertFalse(GroupType.FIXED_POSITIONS.supportsPriority)
        assertTrue(GroupType.TOURNAMENT_RECREATIONAL.supportsPriority)
        assertFalse(GroupType.TOURNAMENT_PRO.supportsPriority)
    }

    @Test
    fun tournamentsHaveNoBalancingMode() {
        assertTrue(GroupType.RECREATIONAL.supportsBalancingMode)
        assertTrue(GroupType.FIXED_POSITIONS.supportsBalancingMode)
        assertFalse(GroupType.TOURNAMENT_RECREATIONAL.supportsBalancingMode)
        assertFalse(GroupType.TOURNAMENT_PRO.supportsBalancingMode)
    }

    @Test
    fun coerceTeamSizeClampsToModeRange() {
        assertEquals(6, GroupType.RECREATIONAL.coerceTeamSize(7))
        assertEquals(7, GroupType.FIXED_POSITIONS.coerceTeamSize(7))
        assertEquals(2, GroupType.FIXED_POSITIONS.coerceTeamSize(1))
        assertEquals(14, GroupType.TOURNAMENT_PRO.coerceTeamSize(20))
    }

    @Test
    fun onlyNonTournamentTypesCanBeConverted() {
        assertTrue(GroupType.RECREATIONAL.canConvertTo(GroupType.FIXED_POSITIONS))
        assertTrue(GroupType.FIXED_POSITIONS.canConvertTo(GroupType.RECREATIONAL))
        assertFalse(GroupType.RECREATIONAL.canConvertTo(GroupType.TOURNAMENT_RECREATIONAL))
        assertFalse(GroupType.TOURNAMENT_PRO.canConvertTo(GroupType.TOURNAMENT_RECREATIONAL))
        // Converter para o próprio tipo é sempre permitido.
        assertTrue(GroupType.TOURNAMENT_PRO.canConvertTo(GroupType.TOURNAMENT_PRO))
    }

    @Test
    fun onlyRecreationalAndFixedPositionsAreSelectable() {
        assertEquals(
            listOf(GroupType.RECREATIONAL, GroupType.FIXED_POSITIONS),
            GroupType.selectableTypes
        )
    }

    @Test
    fun fromStoredValueFallsBackToRecreational() {
        assertEquals(GroupType.FIXED_POSITIONS, GroupType.fromStoredValue("FIXED_POSITIONS"))
        assertEquals(GroupType.RECREATIONAL, GroupType.fromStoredValue(null))
        assertEquals(GroupType.RECREATIONAL, GroupType.fromStoredValue("INVALIDO"))
    }
}
