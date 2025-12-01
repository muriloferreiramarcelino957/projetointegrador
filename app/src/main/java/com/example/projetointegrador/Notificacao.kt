package com.example.projetointegrador.model

data class Notificacao(
    val id: String = "",

    val tipo: String = "",
    val mensagem: String = "",

    val nome_usuario: String = "",
    val endereco: String = "",
    val servico: String = "",
    val data: String = "",
    val hora: String = "",
    val valor: Int = 0,

    val agendamento_id: String = "",
    val data_hora: Long = 0,
    val lido: Boolean = false
)
