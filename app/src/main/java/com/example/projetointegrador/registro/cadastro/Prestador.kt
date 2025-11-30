package com.example.projetointegrador.registro.cadastro

import com.google.firebase.database.IgnoreExtraProperties

@IgnoreExtraProperties
data class Prestador(
    var notaMedia: Double? = 0.0,
    var data_cadastro: String? = "",
    var ultimo_acesso: String? = "",
    var info_prestador: InfoPrestador? = InfoPrestador(),
    var disponibilidade: Map<String, Any?>? = emptyMap(),
    var servicos_oferecidos: Map<String, Any?>? = emptyMap()
)

@IgnoreExtraProperties
data class InfoPrestador(
    var descricao: String? = "",
    var nivel_cadastro: String? = "",
    var quantidade_de_servicos: Long? = 0L
)
