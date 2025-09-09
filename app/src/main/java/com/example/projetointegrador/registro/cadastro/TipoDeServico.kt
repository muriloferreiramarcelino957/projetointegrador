package com.example.projetointegrador.registro.cadastro

import android.os.Bundle
import android.text.Editable
import android.text.InputFilter
import android.text.TextWatcher
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ArrayAdapter
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import com.example.projetointegrador.R
import com.example.projetointegrador.databinding.FragmentTipoDeServico1Binding
import com.google.android.material.timepicker.MaterialTimePicker
import com.google.android.material.timepicker.TimeFormat
import java.util.Locale

class TipoDeServico1Fragment : Fragment() {

    private var _binding: FragmentTipoDeServico1Binding? = null
    private val binding get() = _binding!!

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentTipoDeServico1Binding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)


        binding.backButton.setOnClickListener {
            findNavController().navigateUp()
        }

        val tipos = listOf("Tipo de serviço 1", "Fáxina", "Hidraúlica", "Elétrica")
        binding.autoTipoServico.setAdapter(
            ArrayAdapter(requireContext(), android.R.layout.simple_list_item_1, tipos)
        )
        binding.autoTipoServico.setOnClickListener { binding.autoTipoServico.showDropDown() }

        // Valor p/ hora: apenas 3 dígitos numéricos (XML já limita; reforço via filtro)
        binding.editValorHora.filters = arrayOf(InputFilter.LengthFilter(3))
        binding.editValorHora.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
            override fun afterTextChanged(s: Editable?) {}
            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
                val valorFloat = s?.toString()?.toFloatOrNull() ?: 0f
                // Use 'valorFloat' quando precisar
            }
        })

        setupTimeInput(binding.inputHorario1)
        setupTimeInput(binding.inputHorario2)
        setupTimeInput(binding.inputHorario3)

        binding.btnProximo.setOnClickListener {
            val tipo = binding.autoTipoServico.text?.toString()?.trim().orEmpty()
            val valor = binding.editValorHora.text?.toString()?.toFloatOrNull() ?: 0f
            val h1 = binding.inputHorario1.text?.toString().orEmpty()
            val h2 = binding.inputHorario2.text?.toString().orEmpty()
            val h3 = binding.inputHorario3.text?.toString().orEmpty()
            // TODO: validação/navegação
        }
    }

    private fun setupTimeInput(editText: android.widget.EditText) {
        val openPicker = View.OnClickListener {
            val picker = MaterialTimePicker.Builder()
                .setTimeFormat(TimeFormat.CLOCK_24H)
                .setHour(8)
                .setMinute(0)
                .setTitleText("Selecione um horário")
                .build()

            picker.addOnPositiveButtonClickListener {
                val h = picker.hour
                val m = picker.minute
                editText.setText(String.format(Locale.getDefault(), "%02d:%02d", h, m))
            }
            picker.show(parentFragmentManager, editText.id.toString())
        }
        editText.setOnClickListener(openPicker)
        editText.setOnFocusChangeListener { _, hasFocus -> if (hasFocus) openPicker.onClick(editText) }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}