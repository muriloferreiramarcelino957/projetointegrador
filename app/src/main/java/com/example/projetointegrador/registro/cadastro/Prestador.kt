package com.example.projetointegrador.registro.cadastro

data class Prestador(
    var uid: String = "",
    val dataDeInicio: String = "",
    val notaMedia: Double = 0.0,
    val quantidadeAvaliacoes: Int = 0
)