package com.example.projetointegrador.registro.cadastro

import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.core.view.isVisible
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import com.example.projetointegrador.R
import com.example.projetointegrador.databinding.TelaCadastroBinding
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseAuthInvalidCredentialsException
import com.google.firebase.auth.FirebaseAuthUserCollisionException
import com.google.firebase.auth.FirebaseAuthWeakPasswordException
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.FirebaseDatabase

class TelaCadastroFragment : Fragment() {

    private var _binding: TelaCadastroBinding? = null
    private val binding get() = _binding!!
    private lateinit var auth: FirebaseAuth
    private lateinit var database: DatabaseReference

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = TelaCadastroBinding.inflate(inflater, container, false)
        auth = FirebaseAuth.getInstance()
        database = FirebaseDatabase.getInstance().reference
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
            val username = binding.editTextTextNome.text.toString().trim()
            val email = binding.editTextTextEmail.text.toString().trim()
            val password = binding.editTextTextSenha.text.toString().trim()
            val birthdate = binding.editTextTextNascimento.text.toString().trim()
            val cpf = binding.editTextTextCPF.text.toString().trim()

            if (username.isEmpty() || email.isEmpty() || password.isEmpty() || birthdate.isEmpty() || cpf.isEmpty()) {
                Toast.makeText(requireContext(), "Preencha todos os campos.", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            val userParcial = User(
                nomeUsuario = username,
                email = email,
                dataNascimento = birthdate,
                cpf = cpf,
                cep = "",
                tipoLogradouro = "",
                descLogradouro = "",
                numero = 0,
                bairro = "",
                cidade = "",
                estado = ""
            )
            val action = TelaCadastroFragmentDirections.actionTelaCadastroFragmentToTelaCadastro2(userParcial)
            findNavController().navigate(action)
    }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
