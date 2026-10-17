@file:OptIn(InternalResourceApi::class)

package com.bismarck.voleimanager.app.resources

import kotlin.OptIn
import kotlin.String
import kotlin.collections.MutableMap
import org.jetbrains.compose.resources.DrawableResource
import org.jetbrains.compose.resources.InternalResourceApi
import org.jetbrains.compose.resources.ResourceItem

private const val MD: String = "composeResources/com.bismarck.voleimanager.app.resources/"

public val Res.drawable.arrowdown: DrawableResource by lazy {
      DrawableResource("drawable:arrowdown", setOf(
        ResourceItem(setOf(), "${MD}drawable/arrowdown.xml", -1, -1),
      ))
    }

public val Res.drawable.arrowsbothsides: DrawableResource by lazy {
      DrawableResource("drawable:arrowsbothsides", setOf(
        ResourceItem(setOf(), "${MD}drawable/arrowsbothsides.xml", -1, -1),
      ))
    }

public val Res.drawable.arrowup: DrawableResource by lazy {
      DrawableResource("drawable:arrowup", setOf(
        ResourceItem(setOf(), "${MD}drawable/arrowup.xml", -1, -1),
      ))
    }

public val Res.drawable.bola_volei_fundo_escuro: DrawableResource by lazy {
      DrawableResource("drawable:bola_volei_fundo_escuro", setOf(
        ResourceItem(setOf(), "${MD}drawable/bola_volei_fundo_escuro.xml", -1, -1),
      ))
    }

public val Res.drawable.crown_icon: DrawableResource by lazy {
      DrawableResource("drawable:crown_icon", setOf(
        ResourceItem(setOf(), "${MD}drawable/crown_icon.xml", -1, -1),
      ))
    }

public val Res.drawable.foto_perfil_desenvolvedor: DrawableResource by lazy {
      DrawableResource("drawable:foto_perfil_desenvolvedor", setOf(
        ResourceItem(setOf(), "${MD}drawable/foto_perfil_desenvolvedor.webp", -1, -1),
      ))
    }

public val Res.drawable.logo_volei_manager: DrawableResource by lazy {
      DrawableResource("drawable:logo_volei_manager", setOf(
        ResourceItem(setOf(), "${MD}drawable/logo_volei_manager.xml", -1, -1),
      ))
    }

public val Res.drawable.plus_minus: DrawableResource by lazy {
      DrawableResource("drawable:plus_minus", setOf(
        ResourceItem(setOf(), "${MD}drawable/plus_minus.xml", -1, -1),
      ))
    }

public val Res.drawable.plus_minus_bold: DrawableResource by lazy {
      DrawableResource("drawable:plus_minus_bold", setOf(
        ResourceItem(setOf(), "${MD}drawable/plus_minus_bold.xml", -1, -1),
      ))
    }

public val Res.drawable.point_and_rotation_icon: DrawableResource by lazy {
      DrawableResource("drawable:point_and_rotation_icon", setOf(
        ResourceItem(setOf(), "${MD}drawable/point_and_rotation_icon.xml", -1, -1),
      ))
    }

public val Res.drawable.point_icon: DrawableResource by lazy {
      DrawableResource("drawable:point_icon", setOf(
        ResourceItem(setOf(), "${MD}drawable/point_icon.xml", -1, -1),
      ))
    }

public val Res.drawable.premium_icon3: DrawableResource by lazy {
      DrawableResource("drawable:premium_icon3", setOf(
        ResourceItem(setOf(), "${MD}drawable/premium_icon3.xml", -1, -1),
      ))
    }

public val Res.drawable.setter_icon_en: DrawableResource by lazy {
      DrawableResource("drawable:setter_icon_en", setOf(
        ResourceItem(setOf(), "${MD}drawable/setter_icon_en.xml", -1, -1),
      ))
    }

public val Res.drawable.setter_icon_es: DrawableResource by lazy {
      DrawableResource("drawable:setter_icon_es", setOf(
        ResourceItem(setOf(), "${MD}drawable/setter_icon_es.xml", -1, -1),
      ))
    }

public val Res.drawable.setter_icon_pt: DrawableResource by lazy {
      DrawableResource("drawable:setter_icon_pt", setOf(
        ResourceItem(setOf(), "${MD}drawable/setter_icon_pt.xml", -1, -1),
      ))
    }

public val Res.drawable.volei_manager_icon: DrawableResource by lazy {
      DrawableResource("drawable:volei_manager_icon", setOf(
        ResourceItem(setOf(), "${MD}drawable/volei_manager_icon.xml", -1, -1),
      ))
    }

public val Res.drawable.vs_text: DrawableResource by lazy {
      DrawableResource("drawable:vs_text", setOf(
        ResourceItem(setOf(), "${MD}drawable/vs_text.xml", -1, -1),
      ))
    }

public val Res.drawable.yin_yang_horizontal: DrawableResource by lazy {
      DrawableResource("drawable:yin_yang_horizontal", setOf(
        ResourceItem(setOf(), "${MD}drawable/yin_yang_horizontal.xml", -1, -1),
      ))
    }

public val Res.drawable.yin_yang_vertical: DrawableResource by lazy {
      DrawableResource("drawable:yin_yang_vertical", setOf(
        ResourceItem(setOf(), "${MD}drawable/yin_yang_vertical.xml", -1, -1),
      ))
    }

public val Res.drawable.zzz_rest: DrawableResource by lazy {
      DrawableResource("drawable:zzz_rest", setOf(
        ResourceItem(setOf(), "${MD}drawable/zzz_rest.xml", -1, -1),
      ))
    }

@InternalResourceApi
internal fun _collectCommonMainDrawable0Resources(map: MutableMap<String, DrawableResource>) {
  map.put("arrowdown", Res.drawable.arrowdown)
  map.put("arrowsbothsides", Res.drawable.arrowsbothsides)
  map.put("arrowup", Res.drawable.arrowup)
  map.put("bola_volei_fundo_escuro", Res.drawable.bola_volei_fundo_escuro)
  map.put("crown_icon", Res.drawable.crown_icon)
  map.put("foto_perfil_desenvolvedor", Res.drawable.foto_perfil_desenvolvedor)
  map.put("logo_volei_manager", Res.drawable.logo_volei_manager)
  map.put("plus_minus", Res.drawable.plus_minus)
  map.put("plus_minus_bold", Res.drawable.plus_minus_bold)
  map.put("point_and_rotation_icon", Res.drawable.point_and_rotation_icon)
  map.put("point_icon", Res.drawable.point_icon)
  map.put("premium_icon3", Res.drawable.premium_icon3)
  map.put("setter_icon_en", Res.drawable.setter_icon_en)
  map.put("setter_icon_es", Res.drawable.setter_icon_es)
  map.put("setter_icon_pt", Res.drawable.setter_icon_pt)
  map.put("volei_manager_icon", Res.drawable.volei_manager_icon)
  map.put("vs_text", Res.drawable.vs_text)
  map.put("yin_yang_horizontal", Res.drawable.yin_yang_horizontal)
  map.put("yin_yang_vertical", Res.drawable.yin_yang_vertical)
  map.put("zzz_rest", Res.drawable.zzz_rest)
}
