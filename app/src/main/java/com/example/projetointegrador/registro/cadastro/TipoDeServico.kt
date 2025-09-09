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
        initListeners()

        val tipos = listOf("Tipo de serviço 1", "Fáxina", "Hidraúlica", "Elétrica")
        binding.autoTipoServico.setAdapter(
            ArrayAdapter(requireContext(), android.R.layout.simple_list_item_1, tipos)
        )
        binding.autoTipoServico.setOnClickListener { binding.autoTipoServico.showDropDown() }

        // Valor p/ hora: apenas 3 dígitos numéricos (XML já limita; reforço via filtro)
        binding.editValorHora.filters = arrayOf(InputFilter.LengthFilter(3))

        setupTimeInput(binding.inputHorario1)
        setupTimeInput(binding.inputHorario2)
        setupTimeInput(binding.inputHorario3)
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

    private fun initListeners(){
        binding.backButton.setOnClickListener {
            findNavController().navigateUp()
        }
        binding.btnProximo.setOnClickListener {
            findNavController().navigate(R.id.action_tipoDeServico1Fragment_to_tipoDeServico2)
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}