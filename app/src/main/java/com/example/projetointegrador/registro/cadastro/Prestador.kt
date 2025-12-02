package com.example.projetointegrador.registro.cadastro

data class InfoPrestador(
    var descricao: String? = "",
    var notaMedia: Double? = 0.0,
    var quantidade_de_servicos: Int? = 0
)

data class Prestador(
    var nivel_cadastro: String? = "",
    var ultimo_acesso: String? = "",
    var data_cadastro: String? = "",
    var servicos_oferecidos: List<String?>? = emptyList(),
    var disponibilidade: Map<String, Any?>? = emptyMap(),
    var info_prestador: InfoPrestador? = InfoPrestador()
)


