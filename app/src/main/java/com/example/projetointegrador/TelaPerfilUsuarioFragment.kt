package com.example.projetointegrador

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import com.example.projetointegrador.databinding.TelaDePerfilDoUsuarioBinding
import com.example.projetointegrador.navigation.TopNavigationBarHelper
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.*

class TelaPerfilUsuarioFragment : Fragment() {

    private var _binding: TelaDePerfilDoUsuarioBinding? = null
    private val binding get() = _binding!!

    private lateinit var auth: FirebaseAuth
    private lateinit var database: DatabaseReference
    private lateinit var uid: String

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = TelaDePerfilDoUsuarioBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        TopNavigationBarHelper.setupNavigationBar(binding.root, this)

        auth = FirebaseAuth.getInstance()
        database = FirebaseDatabase.getInstance().reference

        uid = auth.currentUser?.uid ?: return

        carregarUsuario()
        verificarSeEhPrestador()
    }

    // =============================================================
    // 1 — CARREGA DADOS BÁSICOS DO USUÁRIO (nome, local)
    // =============================================================
    private fun carregarUsuario() {
        database.child("usuarios").child(uid)
            .get().addOnSuccessListener { snap ->

                val nome = snap.child("nome").getValue(String::class.java) ?: "Usuário"
                val bairro = snap.child("bairro").getValue(String::class.java) ?: ""
                val cidade = snap.child("cidade").getValue(String::class.java) ?: ""

                binding.txtNome.text = nome
                binding.txtLocal.text =
                    if (bairro.isBlank() && cidade.isBlank()) "Local não informado"
                    else "$bairro - $cidade"
            }
    }

    // =============================================================
    // 2 — VERIFICA SE O USUÁRIO É PRESTADOR
    // =============================================================
    private fun verificarSeEhPrestador() {
        val ref = database.child("prestadores").child(uid)

        ref.get().addOnSuccessListener { snap ->
            if (!snap.exists()) {
                ocultarPartesDePrestador()
                return@addOnSuccessListener
            }
            carregarDadosPrestador(snap)
        }
    }

    // =============================================================
    // 3 — CARREGA INFORMAÇÕES DO PRESTADOR
    // =============================================================
    private fun carregarDadosPrestador(snap: DataSnapshot) {

        // DATA DE CADASTRO
        val desde = snap.child("data-cadastro").getValue(String::class.java) ?: "2025"
        binding.txtDesde.text = "Na AllService desde $desde"

        // --- INFO PRESTADOR ---
        val info = snap.child("info_prestador")

        val descricao = info.child("descricao").getValue(String::class.java) ?: ""
        val nivelCadastro = info.child("nivel_cadastro").getValue(String::class.java) ?: ""
        val quantidadeServicos = info.child("quantidade_de_servicos").getValue(Int::class.java) ?: 0

        binding.txtDescricao.text = descricao
        binding.quantidadeServicos.text = "Quantidade de serviços prestados: $quantidadeServicos"

        // TAGS DE SERVIÇO
        val tags = snap.child("servicos-oferecidos")
            .children
            .mapNotNull { it.key }
            .joinToString(" | ")

        binding.servicosTags.text = if (tags.isNotBlank()) tags else "Nenhum serviço"

        // RATING (a implementar depois)
        binding.txtRating.text = "★ 5,0"

        mostrarPartesDePrestador()
    }

    // =============================================================
    // OCULTAR PARTES EXCLUSIVAS DO PRESTADOR
    // =============================================================
    private fun ocultarPartesDePrestador() {

        // Tags e rating
        binding.servicosTags.visibility = View.GONE
        binding.txtRating.visibility = View.GONE
        binding.ratingMedia.visibility = View.GONE

        // Nível
        binding.labelNivel.visibility = View.GONE
        binding.progressCadastro.visibility = View.GONE
        binding.blocoNivelCadastro.visibility = View.GONE

        // Validações e agenda
        binding.blocoValidacoes.visibility = View.GONE

        // Descrição
        binding.labelDescricao.visibility = View.GONE
        binding.txtDescricao.visibility = View.GONE

        // Fotos
        binding.labelFotos.visibility = View.GONE
        binding.blocoFotos.visibility = View.GONE

        // Quantidade serviços
        binding.quantidadeServicos.visibility = View.GONE
    }

    // =============================================================
    // MOSTRAR PARTES DO PRESTADOR
    // =============================================================
    private fun mostrarPartesDePrestador() {

        binding.ratingMedia.visibility = View.VISIBLE
        binding.txtRating.visibility = View.VISIBLE
        binding.ratingMedia.visibility = View.VISIBLE

        binding.labelNivel.visibility = View.VISIBLE
        binding.progressCadastro.visibility = View.VISIBLE
        binding.blocoNivelCadastro.visibility = View.VISIBLE

        binding.blocoValidacoes.visibility = View.VISIBLE

        binding.labelDescricao.visibility = View.VISIBLE
        binding.txtDescricao.visibility = View.VISIBLE

        binding.labelFotos.visibility = View.VISIBLE
        binding.blocoFotos.visibility = View.VISIBLE

        binding.quantidadeServicos.visibility = View.VISIBLE
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
