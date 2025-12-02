package com.example.projetointegrador.model

data class Notificacao(
    var id: String = "",
    var tipo: String = "",
    var mensagem: String = "",
    var usuario_id: String = "",
    var prestador_id: String = "",
    var agendamento_id: String = "",
    var data: String = "",
    var hora: String = "",
    var data_hora: Long = 0,
    var lido: Boolean = false
)

