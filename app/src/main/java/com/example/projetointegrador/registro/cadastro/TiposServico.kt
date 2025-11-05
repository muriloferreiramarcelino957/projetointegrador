package com.example.projetointegrador.registro.cadastro

import android.os.Parcelable
import kotlinx.parcelize.Parcelize

@Parcelize
data class TiposServico(
    var tipoServico1: String = "",
    var valorServico1: Double = 0.0,
    var horarioServico1_1: String = "",
    var horarioServico1_2: String = "",
    var horarioServico1_3: String = "",
    var tipoServico2: String = "",
    var valorServico2: Double = 0.0,
    var horarioServico2_1: String = "",
    var horarioServico2_2: String = "",
    var horarioServico2_3: String = "",
    var tipoServico3: String = "",
    var valorServico3: Double = 0.0,
    var horarioServico3_1: String = "",
    var horarioServico3_2: String = "",
    var horarioServico3_3: String = ""
) : Parcelable
