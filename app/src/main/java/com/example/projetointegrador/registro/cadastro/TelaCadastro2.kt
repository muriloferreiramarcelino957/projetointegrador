package com.example.projetointegrador.registro.cadastro

import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.core.widget.doOnTextChanged
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import androidx.navigation.fragment.navArgs
import com.example.projetointegrador.R
import com.example.projetointegrador.databinding.FlagPrestadorBinding
import com.example.projetointegrador.databinding.TelaDeCadastro2Binding
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.FirebaseDatabase
import retrofit2.http.GET
import retrofit2.http.Path
import retrofit2.Call
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory

class TelaCadastro2 : Fragment() {

    private var _binding: TelaDeCadastro2Binding? = null
    private val binding get() = _binding!!

    private lateinit var auth: FirebaseAuth
    private lateinit var database: DatabaseReference
    private val args by navArgs<TelaCadastro2Args>()

    // Retrofit (ViaCep)
    private val retrofit = Retrofit.Builder()
        .baseUrl("https://viacep.com.br/ws/")
        .addConverterFactory(GsonConverterFactory.create())
        .build()

    private val service = retrofit.create(ViaCepService::class.java)

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = TelaDeCadastro2Binding.inflate(inflater, container, false)
        auth = FirebaseAuth.getInstance()
        database = FirebaseDatabase.getInstance().reference
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        initListeners()
    }

    private fun initListeners() {

        binding.backButton.setOnClickListener { findNavController().navigateUp() }

        // Auto consulta CEP
        binding.editTextTextCEP.doOnTextChanged { cep, _, _, _ ->
            if ((cep?.length ?: 0) == 8) consultaCep(cep.toString())
        }

        // Clicar em cadastrar
        binding.btnCadastrar.setOnClickListener { mostrarFlag() }
    }

    // --------------------------
    // VIA CEP API
    // --------------------------

    interface ViaCepService {
        @GET("{cep}/json/")
        fun buscaEndereco(@Path("cep") cep: String): Call<EnderecoViaCep>
    }

    private fun consultaCep(cep: String) {
        service.buscaEndereco(cep).enqueue(object : retrofit2.Callback<EnderecoViaCep> {

            override fun onResponse(
                call: Call<EnderecoViaCep>,
                response: retrofit2.Response<EnderecoViaCep>
            ) {
                if (!response.isSuccessful) {
                    toast("Erro no servidor do CEP.")
                    return
                }
                val end = response.body()
                if (end?.erro == true) {
                    toast("CEP não encontrado.")
                    return
                }

                binding.editTextTextDescricaoLogradouro.setText(end?.logradouro)
                binding.editTextTextBairro.setText(end?.bairro)
                binding.editTextCidade.setText(end?.localidade)
                binding.editTextUF.setText(end?.uf)
            }

            override fun onFailure(call: Call<EnderecoViaCep>, t: Throwable) {
                toast("Erro ao consultar CEP: ${t.message}")
            }
        })
    }

    // --------------------------
    // CRIAÇÃO DE CONTA NO FIREBASE
    // --------------------------

    private fun criarConta(onSuccess: () -> Unit) {

        val campos = listOf(
            binding.editTextTextCEP,
            binding.editTextTextDescricaoLogradouro,
            binding.editTextTextNumero,
            binding.editTextTextBairro,
            binding.editTextCidade,
            binding.editTextUF
        )

        // Verifica campos vazios
        if (campos.any { it.text.isNullOrBlank() }) {
            toast("Preencha todos os campos.")
            return
        }

        val user = args.user!!.copy(
            cep = binding.editTextTextCEP.text.toString().trim(),
            logradouro = binding.editTextTextDescricaoLogradouro.text.toString().trim(),
            numero = binding.editTextTextNumero.text.toString().trim(),
            bairro = binding.editTextTextBairro.text.toString().trim(),
            cidade = binding.editTextCidade.text.toString().trim(),
            estado = binding.editTextUF.text.toString().trim()
        )

        binding.btnCadastrar.isEnabled = false

        // 1 — Criar usuário no Auth
        auth.createUserWithEmailAndPassword(user.email, user.senha)
            .addOnSuccessListener { result ->

                val uid = result.user?.uid
                if (uid == null) {
                    binding.btnCadastrar.isEnabled = true
                    toast("Erro inesperado.")
                    return@addOnSuccessListener
                }

                // 2 — Salvar no Realtime Database
                database.child("usuarios").child(uid).setValue(user)
                    .addOnSuccessListener {
                        toast("Usuário cadastrado!")
                        binding.btnCadastrar.isEnabled = true
                        onSuccess()
                    }
                    .addOnFailureListener { e ->
                        binding.btnCadastrar.isEnabled = true
                        toast("Erro ao salvar dados.")
                        Log.e("Cadastro", "Erro: ${e.message}")

                        // evita conta órfã
                        result.user?.delete()
                    }
            }
            .addOnFailureListener { e ->
                binding.btnCadastrar.isEnabled = true
                toast("Erro ao criar conta: ${e.message}")
            }
    }

    // --------------------------
    // OVERLAY FLAG PRESTADOR
    // --------------------------

    private fun mostrarFlag() {
        val flag = FlagPrestadorBinding.inflate(layoutInflater)
        val overlay = flag.root
        overlay.layoutParams = ViewGroup.LayoutParams(
            ViewGroup.LayoutParams.MATCH_PARENT,
            ViewGroup.LayoutParams.MATCH_PARENT
        )

        val root = binding.root
        root.addView(overlay)

        // Se for prestador
        flag.btnSim.setOnClickListener {
            args.user!!.prestador = true
            criarConta {
                findNavController()
                    .navigate(R.id.action_telaCadastro2_to_tipoDeServico1Fragment)
            }
        }

        // Se NÃO for prestador
        flag.btnNao.setOnClickListener {
            criarConta {
                findNavController().navigate(R.id.action_telaCadastro2_to_navigation)
                root.removeView(overlay)
            }
        }
    }

    // --------------------------
    // UTIL
    // --------------------------

    private fun toast(msg: String) {
        Toast.makeText(requireContext(), msg, Toast.LENGTH_SHORT).show()
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
