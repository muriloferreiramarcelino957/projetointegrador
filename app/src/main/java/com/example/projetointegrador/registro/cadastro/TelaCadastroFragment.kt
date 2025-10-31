package com.example.projetointegrador.registro.cadastro

import android.app.DatePickerDialog
import android.os.Bundle
import android.util.Log
import android.util.Patterns
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import com.example.projetointegrador.databinding.TelaCadastroBinding
import java.util.Calendar

class TelaCadastroFragment : Fragment() {

    private var _binding: TelaCadastroBinding? = null
    private val binding get() = _binding!!

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = TelaCadastroBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        initListeners()
    }

    private fun initListeners() {
        binding.backButton.setOnClickListener {
            findNavController().navigateUp()
        }
        binding.button.setOnClickListener {
            verificaInputs()
        }
        binding.editTextTextNascimento.setOnClickListener {
            val calendar = Calendar.getInstance()
            val ano = calendar.get(Calendar.YEAR)
            val mes = calendar.get(Calendar.MONTH)
            val dia = calendar.get(Calendar.DAY_OF_MONTH)

            val datePicker = DatePickerDialog(
                requireContext(),
                { _, year, month, dayOfMonth ->
                    val dataFormatada = String.format("%02d/%02d/%04d", dayOfMonth, month + 1, year)
                    binding.editTextTextNascimento.setText(dataFormatada)
                },
                ano, mes, dia
            )
            val maxCalendar = Calendar.getInstance().apply { add(Calendar.YEAR, -18) }
            datePicker.datePicker.maxDate = maxCalendar.timeInMillis
            datePicker.show()
        }
    }

    private fun verificaInputs() {
        val nome = binding.editTextTextNome.text.toString().trim()
        val email = binding.editTextTextEmail.text.toString().trim()
        val cpf = binding.editTextTextCPF.text.toString().replace(Regex("\\D"), "")
        val dataDeNascimento = binding.editTextTextNascimento.text.toString().trim()
        val senha = binding.editTextTextSenha.text?.toString()?.trim().orEmpty()
        val senhaRepetida = binding.editTextTextRepitaSenha.text?.toString()?.trim().orEmpty()
        if (nome.length < 4) {
            Toast.makeText(requireContext(), "O nome precisa ter no mínimo 4 caracteres", Toast.LENGTH_SHORT).show()
            return
        }
        if (!validarEmail(email)) {
            Toast.makeText(requireContext(), "Insira um email real", Toast.LENGTH_SHORT).show()
            return
        }
        if (!validarCPF(cpf)) {
            Toast.makeText(requireContext(), "Insira um CPF real", Toast.LENGTH_SHORT).show()
            return
        }
        val anoNascimento = dataDeNascimento.takeLast(4).toInt()
        val anoAtual = Calendar.getInstance().get(Calendar.YEAR)
        if (anoNascimento > (anoAtual.toInt() - 18)){
            Toast.makeText(requireContext(), "O cadastro não é permitido para menores de 18 anos", Toast.LENGTH_SHORT).show()
            return
        }
        if (!validarSenha(senha, senhaRepetida)){
            return
        }
        mandarDados()
    }

    private fun validarEmail(email: String): Boolean {
        return Patterns.EMAIL_ADDRESS.matcher(email).matches()
    }

    private fun validarCPF(cpf: String): Boolean {
        if (cpf.length != 11) return false
        if (cpf.all { it == cpf[0] }) return false
        try {
            val numbers = cpf.map { it.toString().toInt() }

            val dv1 = calculateVerifierDigit(numbers.subList(0, 9), 10)
            if (numbers[9] != dv1) return false

            val dv2 = calculateVerifierDigit(numbers.subList(0, 10), 11)
            if (numbers[10] != dv2) return false

            return true
        } catch (e: Exception) {
            return false
        }
    }

    fun calculateVerifierDigit(digits: List<Int>, weight: Int): Int {
        var sum = 0
        for (i in digits.indices) {
            sum += digits[i] * (weight - i)
        }
        val remainder = sum % 11
        return if (remainder < 2) 0 else 11 - remainder
    }

    private fun validarSenha(senha: String, senhaRepetida: String): Boolean{
        if (senha.length < 8) {
            Toast.makeText(requireContext(), "A senha precisa ter no mínimo 8 caracteres", Toast.LENGTH_SHORT).show()
            return false
        }
        val regex = Regex("^(?=.*[A-Z])(?=.*\\d)(?=.*[@#\$%^&+=!])\$")
        val apenasNormais = senha.matches(Regex("^[A-Za-z0-9@#\$%^&+=!]*\$"))
        if (!apenasNormais || !regex.containsMatchIn(senha)){
            Toast.makeText(requireContext(), "A senha deve conter ao menos uma letra maiúscula, um número e um caractere especial", Toast.LENGTH_SHORT).show()
            return false
        }
        if (senha != senhaRepetida) {
            Toast.makeText(requireContext(), "As senhas precisam ser iguais", Toast.LENGTH_SHORT).show()
            Log.d("senha", binding.editTextTextSenha.text?.toString().orEmpty())
            Log.d("senhaRepetida", binding.editTextTextRepitaSenha.text?.toString().orEmpty())
            return false
        }
        return true
    }
    private fun mandarDados(){
        val username = binding.editTextTextNome.text.toString().trim()
        val email = binding.editTextTextEmail.text.toString().trim()
        val password = binding.editTextTextSenha.text.toString().trim()
        val birthdate = binding.editTextTextNascimento.text.toString().trim()
        val cpf = binding.editTextTextCPF.text.toString().trim()

        if (username.isEmpty() || email.isEmpty() || password.isEmpty() || birthdate.isEmpty() || cpf.isEmpty()) {
            Toast.makeText(requireContext(), "Preencha todos os campos.", Toast.LENGTH_SHORT).show()
            return
        }

        val userParcial = User(
            nomeUsuario = username,
            email = email,
            dataNascimento = birthdate,
            cpf = cpf,
            senha = password,
            cep = "",
            logradouro = "",
            numero = "",
            bairro = "",
            cidade = "",
            estado = "",
            prestador = false
        )
        val action = TelaCadastroFragmentDirections.actionTelaCadastroFragmentToTelaCadastro2(userParcial)
        findNavController().navigate(action)
    }
    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
