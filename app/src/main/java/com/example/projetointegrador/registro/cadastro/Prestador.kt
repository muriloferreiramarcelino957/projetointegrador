package com.example.projetointegrador.registro.cadastro

data class Prestador(
    val descricao: String? = null,
    val nivel_cadastro: String? = null,
    val quantidade_de_servicos: Int? = 0,
    val notaMedia: Double? = 0.0,
    val servicos_oferecidos: Map<String, Int>? = null
)


