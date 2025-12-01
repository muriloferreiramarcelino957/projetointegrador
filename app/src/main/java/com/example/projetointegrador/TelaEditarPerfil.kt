package com.example.projetointegrador

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.Toast
import androidx.core.view.GravityCompat
import androidx.drawerlayout.widget.DrawerLayout
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import com.example.projetointegrador.databinding.TelaDeEditarPerfilBinding
import com.example.projetointegrador.navigation.TopNavigationBarHelper
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore

class TelaEditarPerfil : Fragment() {

    private var _binding: TelaDeEditarPerfilBinding? = null
    private val binding get() = _binding!!

    // Firestore e Auth
    private val db = FirebaseFirestore.getInstance()
    private val auth = FirebaseAuth.getInstance()

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = TelaDeEditarPerfilBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        TopNavigationBarHelper.setupNavigationBar(binding.root, this)
        initListeners()
        carregarDadosUsuario()
    }

    private fun initListeners() {
        binding.backButton.setOnClickListener {
            findNavController().navigateUp()
        }

        binding.btnSalvar.setOnClickListener {
            val nome = binding.etNome.text.toString().trim()
            val descricao = binding.etDescricao.text.toString().trim()

            if (nome.isEmpty() || descricao.isEmpty()) {
                Toast.makeText(requireContext(), "Preencha todos os campos!", Toast.LENGTH_SHORT).show()
            } else {
                salvarNoFirestore(nome, descricao)
            }
        }
    }

    // Carrega os dados do Firestore para preencher os campos
    private fun carregarDadosUsuario() {
        val usuarioId = auth.currentUser?.uid ?: return

        db.collection("usuarios").document(usuarioId).get()
            .addOnSuccessListener { document ->
                if (document.exists()) {
                    binding.etNome.setText(document.getString("nome"))
                    binding.etDescricao.setText(document.getString("descricao"))
                }
            }
            .addOnFailureListener {
                Toast.makeText(requireContext(), "Erro ao carregar dados!", Toast.LENGTH_SHORT).show()
            }
    }

    // Salva os dados no Firestore
    private fun salvarNoFirestore(nome: String, descricao: String) {
        val usuarioId = auth.currentUser?.uid ?: return
        val dados = hashMapOf(
            "nome" to nome,
            "descricao" to descricao
        )

        db.collection("usuarios").document(usuarioId)
            .set(dados)
            .addOnSuccessListener {
                Toast.makeText(requireContext(), "Edições salvas!", Toast.LENGTH_SHORT).show()
                findNavController().navigateUp()
            }
            .addOnFailureListener {
                Toast.makeText(requireContext(), "Erro ao salvar alterações!", Toast.LENGTH_SHORT).show()
            }
    }

    private fun configurarMenuLateral() {
        val btnMenu = binding.topBar.root.findViewById<ImageView>(R.id.ic_menu)
        val drawerLayout = binding.root.findViewById<DrawerLayout>(R.id.drawerLayout)

        btnMenu.setOnClickListener {
            drawerLayout.openDrawer(GravityCompat.START)
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
