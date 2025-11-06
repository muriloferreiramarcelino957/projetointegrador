package com.example.projetointegrador.registro.login

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import com.example.projetointegrador.R
import com.example.projetointegrador.databinding.TelaLoginBinding
import com.google.firebase.auth.FirebaseAuth

class TelaLoginFragment : Fragment() {

    private var _binding: TelaLoginBinding? = null
    private val binding get() = _binding!!

    private lateinit var auth: FirebaseAuth

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = TelaLoginBinding.inflate(inflater, container, false)
        auth = FirebaseAuth.getInstance()
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        initListeners()
    }

    private fun initListeners() {
        // Botão de voltar
        binding.backButton.setOnClickListener {
            findNavController().navigateUp()
        }

        // Esqueci a senha
        binding.forgotPasswordText.setOnClickListener {
            findNavController().navigate(R.id.action_telaLoginFragment_to_recuperacao1)
        }

        // Botão login
        binding.loginButton.setOnClickListener {
            realizarLogin()
        }
    }

    private fun realizarLogin() {
        val email = binding.usernameEditText.text.toString().trim()
        val senha = binding.passwordEditText.text.toString().trim()

        if (email.isEmpty() || senha.isEmpty()) {
            Toast.makeText(requireContext(), "Preencha todos os campos", Toast.LENGTH_SHORT).show()
            return
        }

        binding.loginButton.isEnabled = false

        // Login direto com FirebaseAuth
        auth.signInWithEmailAndPassword(email, senha)
            .addOnSuccessListener {
                Toast.makeText(requireContext(), "Login realizado com sucesso!", Toast.LENGTH_SHORT).show()
                findNavController().navigate(R.id.action_telaLoginFragment_to_navigation)
            }
            .addOnFailureListener { e ->
                binding.loginButton.isEnabled = true
                Toast.makeText(requireContext(), "Erro no login: ${e.message}", Toast.LENGTH_SHORT).show()
            }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
