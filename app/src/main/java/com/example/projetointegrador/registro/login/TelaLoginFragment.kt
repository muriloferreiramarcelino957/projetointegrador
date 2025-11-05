package com.example.projetointegrador.registro.login

import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import com.example.projetointegrador.R
import com.example.projetointegrador.databinding.TelaLoginBinding
import com.example.projetointegrador.registro.cadastro.User
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.FirebaseDatabase

class TelaLoginFragment : Fragment() {

    private var _binding: TelaLoginBinding? = null
    private val binding get() = _binding!!
    private lateinit var auth: FirebaseAuth
    private lateinit var database: com.google.firebase.database.DatabaseReference

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = TelaLoginBinding.inflate(inflater, container, false)
        auth = FirebaseAuth.getInstance()
        database = FirebaseDatabase.getInstance().reference
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        initListeners()
    }

    private fun initListeners(){
        binding.backButton.setOnClickListener {
            findNavController().navigateUp()
        }
        
        binding.forgotPasswordText.setOnClickListener {
            findNavController().navigate(R.id.action_telaLoginFragment_to_recuperacao1)
        }
        
        binding.loginButton.setOnClickListener {
            realizarLogin()
        }
    }

    private fun realizarLogin() {
        val nomeUsuario = binding.usernameEditText.text.toString().trim()
        val senha = binding.passwordEditText.text.toString().trim()

        if (nomeUsuario.isEmpty() || senha.isEmpty()) {
            Toast.makeText(requireContext(), "Preencha todos os campos", Toast.LENGTH_SHORT).show()
            return
        }

        binding.loginButton.isEnabled = false

        // Busca o usuário no Realtime Database pelo nome de usuário
        database.child("usuarios")
            .get()
            .addOnSuccessListener { snapshot ->
                if (!snapshot.exists()) {
                    binding.loginButton.isEnabled = true
                    Toast.makeText(requireContext(), "Nenhum usuário encontrado", Toast.LENGTH_SHORT).show()
                    return@addOnSuccessListener
                }

                var usuarioEncontrado = false
                var userData: User? = null
                var uid: String? = null

                // Itera pelos usuários no banco
                for (userSnapshot in snapshot.children) {
                    val user = userSnapshot.getValue(User::class.java)
                    if (user != null) {
                        // Verifica se o nome de usuário corresponde
                        if (user.nomeUsuario.equals(nomeUsuario, ignoreCase = true) && user.senha == senha) {
                            usuarioEncontrado = true
                            userData = user
                            uid = userSnapshot.key
                            break
                        }
                    }
                }

                if (usuarioEncontrado && userData != null && uid != null) {
                    // Login bem-sucedido - autentica com Firebase Auth usando o email
                    auth.signInWithEmailAndPassword(userData.email, senha)
                        .addOnSuccessListener {
                            Toast.makeText(requireContext(), "Login realizado com sucesso!", Toast.LENGTH_SHORT).show()
                            findNavController().navigate(R.id.action_telaLoginFragment_to_navigation)
                        }
                        .addOnFailureListener { e ->
                            // Se não conseguir autenticar com Firebase Auth, ainda permite login
                            Log.w("Login", "Erro ao autenticar com Firebase Auth: ${e.message}")
                            Toast.makeText(requireContext(), "Login realizado com sucesso!", Toast.LENGTH_SHORT).show()
                            findNavController().navigate(R.id.action_telaLoginFragment_to_navigation)
                        }
                } else {
                    binding.loginButton.isEnabled = true
                    Toast.makeText(requireContext(), "Usuário ou senha incorretos", Toast.LENGTH_SHORT).show()
                }
            }
            .addOnFailureListener { e ->
                binding.loginButton.isEnabled = true
                Toast.makeText(requireContext(), "Erro ao conectar: ${e.message}", Toast.LENGTH_SHORT).show()
                Log.e("Login", "Erro ao buscar usuário: ${e.message}")
            }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}