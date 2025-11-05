package com.example.projetointegrador.registro.cadastro

import android.os.Bundle
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.core.widget.doOnTextChanged
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
    val retrofit = Retrofit.Builder()
        .baseUrl("https://viacep.com.br/ws/")
        .addConverterFactory(GsonConverterFactory.create())
        .build()

    val service = retrofit.create(ViaCepService::class.java)

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        _binding = TelaDeCadastro2Binding.inflate(inflater, container, false)
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
        binding.editTextTextCEP.doOnTextChanged(){ cep, _, _, _ ->
            if (cep?.length == 8) {
                consultaCep(cep.toString().trim())
            }
        }
        binding.btnCadastrar.setOnClickListener {
            mostrarFlag()
        }
    }

    interface ViaCepService {
        @GET("{cep}/json/")
        fun buscaEndereco(@Path("cep") cep: String): Call<EnderecoViaCep>
    }

    fun consultaCep(cep: String) {
        val call = service.buscaEndereco(cep.replace("\\D".toRegex(), ""))
        call.enqueue(object : retrofit2.Callback<EnderecoViaCep> {
            override fun onResponse(
                call: retrofit2.Call<EnderecoViaCep>,
                response: retrofit2.Response<EnderecoViaCep>
            ) {
                if (response.isSuccessful) {
                    val endereco = response.body()
                    if (endereco?.erro == true) {
                        Toast.makeText(context, "CEP não encontrado.", Toast.LENGTH_SHORT).show()
                    } else {
                        binding.editTextTextDescricaoLogradouro.setText(endereco?.logradouro)
                        binding.editTextTextBairro.setText(endereco?.bairro)
                        binding.editTextCidade.setText(endereco?.localidade)
                        binding.editTextUF.setText(endereco?.uf)
                    }
                } else {
                    Toast.makeText(context, "Erro na resposta do servidor.", Toast.LENGTH_SHORT).show()
                }
            }

            override fun onFailure(call: retrofit2.Call<EnderecoViaCep>, t: Throwable) {
                Toast.makeText(context, "Falha na requisição: ${t.message}", Toast.LENGTH_SHORT).show()
            }
        })
    }
    private fun criarConta(onSuccess: () -> Unit) {
        val cep = binding.editTextTextCEP.text.toString().trim()
        val descLogradouro = binding.editTextTextDescricaoLogradouro.text.toString().trim()
        val numero = binding.editTextTextNumero.text.toString().trim()
        val bairro = binding.editTextTextBairro.text.toString().trim()
        val cidade = binding.editTextCidade.text.toString().trim()
        val estado = binding.editTextUF.text.toString().trim()

        if (cep.isEmpty() || descLogradouro.isEmpty() || numero.isEmpty() || bairro.isEmpty() || cidade.isEmpty() || estado.isEmpty()) {
            Toast.makeText(requireContext(), "Preencha todos os campos.", Toast.LENGTH_SHORT).show()
            return@criarConta
        }

        val user = args.user!!
        user.cep = cep
        user.logradouro = descLogradouro
        user.numero = numero
        user.bairro = bairro
        user.cidade = cidade
        user.estado = estado

        binding.btnCadastrar.isEnabled = false


        auth.createUserWithEmailAndPassword(user.email , user.senha)
            .addOnSuccessListener { task ->
                val uid = task.user?.uid
                if (uid == null) {
                    binding.btnCadastrar.isEnabled = true
                    Toast.makeText(requireContext(), "Erro ao criar usuário.", Toast.LENGTH_SHORT).show()
                    return@addOnSuccessListener
                }
                database.child("usuarios").child(uid).setValue(user)
                    .addOnSuccessListener {
                        Toast.makeText(requireContext(), "Usuário cadastrado com sucesso.", Toast.LENGTH_SHORT).show()
                        binding.btnCadastrar.isEnabled = true
                        onSuccess()
                    }
                    .addOnFailureListener { e ->
                        binding.btnCadastrar.isEnabled = true
                        Toast.makeText(requireContext(), "Erro ao salvar dados. Tente novamente.", Toast.LENGTH_SHORT).show()
                        Log.e("Erro", "Erro ao salvar dados do usuário: ${e.message}")
                        
                        // Se falhar ao salvar dados, tenta deletar o usuário do Auth para evitar conta órfã
                        task.user?.delete()?.addOnCompleteListener {
                            Log.d("Cadastro", "Usuário órfão removido do Auth após falha ao salvar dados")
                        }
                    }
            }
            .addOnFailureListener { e ->
                binding.btnCadastrar.isEnabled = true
                Toast.makeText(requireContext(), "Erro ao criar usuário: ${e.message}", Toast.LENGTH_SHORT).show()
                Log.e("Cadastro", "Erro ao criar usuário no Auth: ${e.message}")
            }
    }
    private fun mostrarFlag(){
        val flag = FlagPrestadorBinding.inflate(layoutInflater)
        val overlay = flag.root
        overlay.layoutParams = ViewGroup.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT)
        val root = binding.root
        root.addView(overlay)

        flag.btnSim.setOnClickListener {
            args.user!!.prestador = true
            criarConta(){
                findNavController().navigate(R.id.action_telaCadastro2_to_tipoDeServico1Fragment)
            }
        }
        flag.btnNao.setOnClickListener {
            criarConta(){
                Toast.makeText(requireContext(), "Ir para tela principal", Toast.LENGTH_SHORT).show()
                root.removeView(overlay)
            }
        }
    }
    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}