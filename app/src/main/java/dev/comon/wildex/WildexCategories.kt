package dev.comon.wildex

enum class WildexCategory(
    val titleText: String,
    val koreanName: String,
    val subtitleText: String,
) {
    BIRDS("Birds", "조류", "Aves Classis"),
    MAMMALS("Mammals", "포유류", "Mammalia Classis"),
    INSECTS("Insects", "곤충", "Insecta Classis"),
    PLANTS("Plants", "식물", "Plantae Kingdom"),
}
