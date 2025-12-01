package com.example.projetointegrador.registro.cadastro

import android.app.DatePickerDialog
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.util.Patterns
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.EditText
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import com.example.projetointegrador.databinding.TelaCadastroBinding
import java.util.Calendar

class TelaCadastroFragment : Fragment() {

    private var _binding: TelaCadastroBinding? = null
    private val binding get() = _binding!!

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?
    ): View {
        _binding = TelaCadastroBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        initListeners()

        // ----------- MÁSCARA DE TELEFONE -----------
        binding.editTextTelefone.addTextChangedListener(
            TelefoneMask(binding.editTextTelefone)
        )
        binding.editTextFacebook.addTextChangedListener(object : TextWatcher {
            private var isUpdating = false

            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {}

            override fun afterTextChanged(s: Editable?) {
                if (isUpdating) return

                val texto = s.toString()

                if (texto.isNotEmpty() && !texto.startsWith("@")) {
                    isUpdating = true
                    binding.editTextFacebook.setText("@$texto")
                    binding.editTextFacebook.setSelection(binding.editTextFacebook.text!!.length)
                    isUpdating = false
                }
            }
        })

    }

    // -------------------------------------------------------------------------
    //  LISTENERS
    // -------------------------------------------------------------------------
    private fun initListeners() = with(binding) {

        backButton.setOnClickListener { findNavController().navigateUp() }

        button.setOnClickListener { verificaInputs() }

        editTextTextNascimento.setOnClickListener { abrirDatePicker() }
    }

    private fun abrirDatePicker() {
        val calendar = Calendar.getInstance()
        val ano = calendar.get(Calendar.YEAR)
        val mes = calendar.get(Calendar.MONTH)
        val dia = calendar.get(Calendar.DAY_OF_MONTH)

        val picker = DatePickerDialog(
            requireContext(),
            { _, year, month, day ->
                binding.editTextTextNascimento.setText(
                    "%02d/%02d/%04d".format(day, month + 1, year)
                )
            }, ano, mes, dia
        )

        val limite = Calendar.getInstance().apply { add(Calendar.YEAR, -18) }
        picker.datePicker.maxDate = limite.timeInMillis

        picker.show()
    }

    // -------------------------------------------------------------------------
    //  VALIDAÇÃO
    // -------------------------------------------------------------------------
    private fun verificaInputs() = with(binding) {

        val nome = editTextTextNome.text.toString().trim()
        val email = editTextTextEmail.text.toString().trim()
        val cpf = editTextTextCPF.text.toString().replace(Regex("\\D"), "")
        val nascimento = editTextTextNascimento.text.toString().trim()
        val senha = editTextTextSenha.text?.toString()?.trim().orEmpty()
        val senha2 = editTextTextRepitaSenha.text?.toString()?.trim().orEmpty()
        val telefone = editTextTelefone.text.toString().trim()
        val facebook = editTextFacebook.text.toString().trim()

        if (listOf(nome, email, telefone, facebook, cpf, nascimento, senha, senha2).any { it.isEmpty() }) {
            toast("Preencha todos os campos.")
            return
        }

        if (nome.length < 4) {
            toast("O nome precisa ter no mínimo 4 caracteres.")
            return
        }

        if (!Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
            toast("Insira um email válido.")
            return
        }

        // TELEFONE PRECISA TER 11 DÍGITOS
        val telefoneDigits = telefone.filter { it.isDigit() }
        if (telefoneDigits.length != 11) {
            toast("O telefone precisa ter 11 dígitos (DDD + número).")
            return
        }

        if (!validarCPF(cpf)) {
            toast("Insira um CPF real.")
            return
        }

        if (!maiorDeIdade(nascimento)) {
            toast("Cadastro permitido somente para maiores de 18 anos.")
            return
        }

        if (!validarSenha(senha, senha2)) return

        mandarDados()
    }

    private fun validarCPF(cpf: String): Boolean {
        if (cpf.length != 11) return false
        if (cpf.all { it == cpf[0] }) return false

        return try {
            val numeros = cpf.map { it.toString().toInt() }

            val dv1 = calcularDV(numeros.subList(0, 9), 10)
            if (numeros[9] != dv1) return false

            val dv2 = calcularDV(numeros.subList(0, 10), 11)
            if (numeros[10] != dv2) return false

            true
        } catch (_: Exception) {
            false
        }
    }

    private fun calcularDV(digitos: List<Int>, pesoInicial: Int): Int {
        val soma = digitos.indices.sumOf { digitos[it] * (pesoInicial - it) }
        val resto = soma % 11
        return if (resto < 2) 0 else 11 - resto
    }

    private fun maiorDeIdade(data: String): Boolean {
        val anoNasc = data.takeLast(4).toIntOrNull() ?: return false
        val anoAtual = Calendar.getInstance().get(Calendar.YEAR)
        return anoNasc <= anoAtual - 18
    }

    private fun validarSenha(senha: String, repetir: String): Boolean {
        if (senha.length < 8) {
            toast("A senha precisa ter no mínimo 8 caracteres.")
            return false
        }

        if (senha.contains(" ")) {
            toast("A senha não pode conter espaços.")
            return false
        }

        val regexComplexidade = Regex("^(?=.*[A-Z])(?=.*\\d)(?=.*[@#\$%^&+=!]).+$")
        val somenteValidos = Regex("^[A-Za-z0-9@#\$%^&+=!]*$")

        if (!somenteValidos.matches(senha) || !regexComplexidade.containsMatchIn(senha)) {
            toast("A senha deve conter letra maiúscula, número e caractere especial.")
            return false
        }

        if (senha != repetir) {
            toast("As senhas precisam ser iguais.")
            return false
        }

        return true
    }

    // -------------------------------------------------------------------------
    //  ENVIO PARA PRÓXIMA TELA
    // -------------------------------------------------------------------------
    private fun mandarDados() = with(binding) {
        val nomeCorrigido = editTextTextNome.text.toString()
            .lowercase()
            .replaceFirstChar { it.uppercase() }

        val user = User(
            nome = nomeCorrigido,
            email = editTextTextEmail.text.toString().trim(),
            dataNascimento = editTextTextNascimento.text.toString().trim(),
            cpf = editTextTextCPF.text.toString().trim(),
            senha = editTextTextSenha.text.toString().trim(),
            telefone = editTextTelefone.text.toString().trim(),
            facebook = editTextFacebook.text.toString().trim(),
            cep = "",
            logradouro = "",
            numero = "",
            bairro = "",
            cidade = "",
            estado = "",
            prestador = false
        )

        val action =
            TelaCadastroFragmentDirections.actionTelaCadastroFragmentToTelaCadastro2(user)

        findNavController().navigate(action)
    }

    private fun toast(msg: String) =
        Toast.makeText(requireContext(), msg, Toast.LENGTH_SHORT).show()

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}

class TelefoneMask(private val editText: EditText) : TextWatcher {

    private var isUpdating = false

    override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}

    override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {}

    override fun afterTextChanged(s: Editable?) {
        if (isUpdating) return

        val digits = s.toString().filter { it.isDigit() }

        val formatted = when {
            digits.length >= 11 ->
                "(${digits.substring(0,2)}) ${digits.substring(2,7)}-${digits.substring(7,11)}"

            digits.length >= 7 ->
                "(${digits.substring(0,2)}) ${digits.substring(2,6)}-${digits.substring(6)}"

            digits.length >= 3 ->
                "(${digits.substring(0,2)}) ${digits.substring(2)}"

            digits.length >= 1 ->
                "(${digits}"

            else -> ""
        }

        isUpdating = true
        editText.setText(formatted)
        editText.setSelection(formatted.length)
        isUpdating = false
    }
}