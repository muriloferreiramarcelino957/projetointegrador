package com.example.projetointegrador.registro.cadastro

data class EnderecoViaCep(
    val cep: String? = null,
    val logradouro: String? = null,
    val bairro: String? = null,
    val localidade: String? = null,
    val uf: String? = null,
    val erro: Boolean? = null
)
