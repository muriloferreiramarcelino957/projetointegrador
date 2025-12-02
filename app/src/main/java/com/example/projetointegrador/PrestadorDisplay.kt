package com.example.projetointegrador

data class PrestadorDisplay(
    val uid: String = "",
    val nome: String = "",
    val cidade: String = "",
    val info_prestador: InfoPrestador = InfoPrestador(),
    val servicos: List<String> = emptyList(),

    val data_cadastro: String = "",
    val ultimo_acesso: String = ""
)

data class InfoPrestador(
    val descricao: String = "",
    val notaMedia: Double = 0.0,
    val quantidade_de_servicos: Int = 0,
    val nivel_cadastro: String = ""
)
