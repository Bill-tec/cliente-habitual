package com.br.clientehabitual;

import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.RadioButton;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;

import com.br.clientehabitual.banco.daos.ClienteDAO;
import com.br.clientehabitual.banco.daos.InadimplenciaDAO;
import com.br.clientehabitual.banco.daos.ProdutoDAO;
import com.br.clientehabitual.models.Cliente;
import com.br.clientehabitual.models.Inadimplencia;
import com.br.clientehabitual.models.Produto;
import com.br.clientehabitual.util.Conversoes;
import com.github.rtoshiro.util.format.SimpleMaskFormatter;
import com.github.rtoshiro.util.format.pattern.MaskPattern;
import com.github.rtoshiro.util.format.text.MaskTextWatcher;
import com.google.android.gms.ads.AdListener;
import com.google.android.gms.ads.AdRequest;
import com.google.android.gms.ads.AdView;
import com.google.android.gms.ads.InterstitialAd;
import com.google.android.gms.ads.MobileAds;
import com.google.android.gms.ads.initialization.InitializationStatus;
import com.google.android.gms.ads.initialization.OnInitializationCompleteListener;
import com.google.android.material.floatingactionbutton.FloatingActionButton;

import java.text.DecimalFormat;
import java.util.Calendar;

public class ActivityInadimplencia extends AppCompatActivity {
    private EditText edt_valor, edt_nome, edt_email, edt_data;
    private FloatingActionButton fab_produtos, fab_quitar, fab_edit, fab_nova_inad, fab_expand;
    private TextView edittext_var, txt_inicio, txt_pagamento, txt_nome, txt_total,txt_total_pop;
    private Conversoes converter = new Conversoes();
    private Cliente cliente;
    private Inadimplencia inadimplencia;
    private ClienteDAO clienteDAO = new ClienteDAO(this);
    private InadimplenciaDAO inadimplenciaDAO = new InadimplenciaDAO(this);
    private DecimalFormat df = new DecimalFormat("#0.00");
    private Boolean visivel = false;
    private InterstitialAd interstitialAd;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_inadimplencia);

        Intent intent = getIntent();
        int id = Integer.parseInt(intent.getStringExtra("id"));
        cliente = new Cliente(id,"","");
        cliente = clienteDAO.getClienteId(cliente);
        setTitle("Inadimplência");

        txt_nome = findViewById(R.id.cliente_nome);
        txt_nome.setText(cliente.getNome());
        txt_nome.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                popupAtualizarCliente();
            }
        });
        gerarAdTelaToda();

        MobileAds.initialize(this, new OnInitializationCompleteListener() {
            @Override
            public void onInitializationComplete(InitializationStatus initializationStatus) {
            }
        });
        AdView adView = findViewById(R.id.adViewBannerInad);
        adView.loadAd(new AdRequest.Builder().build());


        inadimplencia = inadimplenciaDAO.getInadimpleciaCliente(cliente);
        txt_pagamento = findViewById(R.id.data_pagamento);
        if (inadimplencia != null) {
            txt_total = findViewById(R.id.total_inadimplencia);
            if (inadimplencia.getTotal() > 0){
                txt_total.setText("R$ "+df.format(inadimplencia.getTotal()));
            }

            txt_inicio = findViewById(R.id.data_inicio);
            txt_inicio.setText(converter.calendarToString(inadimplencia.getDataInicio()));
            if (inadimplencia.getDataFim() != null){
                txt_pagamento.setText(converter.calendarToString(inadimplencia.getDataFim()));
                if (inadimplencia.isQuitada()){
                    txt_total.setTextColor(Color.GREEN);
                    txt_pagamento.setTextColor(Color.GREEN);
                } else {
                    Calendar calendar = Calendar.getInstance();
                    if (calendar.getTime().after(inadimplencia.getDataFim().getTime())){
                        txt_total.setTextColor(Color.RED);
                        txt_pagamento.setTextColor(Color.RED);
                    }
                }
            }
        }
        txt_pagamento.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                dataPagamentoPopUp();
            }
        });
        edt_valor = findViewById(R.id.valor_edittext);
        Button btn_salvar = findViewById(R.id.btn_add);
        btn_salvar.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                RadioButton radioButton_acres = findViewById(R.id.RB_acrescentar);
                if (edt_valor.getText().toString().trim().length() > 0){
                    if (edt_valor.getText().toString().trim().equals(".")){
                        Toast.makeText(getApplicationContext(),"Insira um valor",Toast.LENGTH_SHORT).show();
                    } if (inadimplencia.isQuitada() || inadimplencia.getTotal() <= 0){
                        confirmNovaInadimplencia();
                        txt_total.setText("R$ "+df.format(inadimplencia.getTotal()));
                    }
                    else if (radioButton_acres.isChecked()){
                        inadimplencia.setTotal(inadimplencia.getTotal() + Float.parseFloat(edt_valor.getText().toString().trim()));
                        inadimplenciaDAO.setValorTotal(inadimplencia);
                        txt_total.setText("R$ "+df.format(inadimplencia.getTotal()));
                        edt_valor.setText("");
                    } else{
                        if (inadimplencia.getTotal() <= Float.parseFloat(edt_valor.getText().toString().trim())){
                            Toast.makeText(getApplicationContext(), "Inadimplência quitada!",Toast.LENGTH_SHORT).show();
                            txt_total.setText("Troco R$ "+ (Float.parseFloat(edt_valor.getText().toString().trim()) - inadimplencia.getTotal()));
                            txt_total.setTextColor(Color.GREEN);
                            inadimplencia.setQuitada(true);
                            inadimplenciaDAO.setQuitInadimplencia(inadimplencia);
                        } else {
                            inadimplencia.setTotal(inadimplencia.getTotal() - Float.parseFloat(edt_valor.getText().toString().trim()));
                            inadimplenciaDAO.setValorTotal(inadimplencia);
                            txt_total.setText("R$ "+df.format(inadimplencia.getTotal()));
                        }
                    }
                } else {
                    Toast.makeText(getApplicationContext(),"Insira um valor",Toast.LENGTH_SHORT).show();
                }
            }
        });

        /** ####MENU BOTÃO FLUTUANTE #####*/

        fab_nova_inad = findViewById(R.id.fab_nova_inad);
        fab_nova_inad.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                hideShowMenuFab();
                if (!inadimplencia.isQuitada()){
                    Toast.makeText(getApplicationContext(),"ATENÇÃO! A inadimplência ainda esta ativa", Toast.LENGTH_LONG).show();
                }
                confirmNovaInadimplencia();
            }
        });

        fab_produtos = findViewById(R.id.fab_produtos);
        fab_produtos.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                hideShowMenuFab();
                confirmAddProdutosInad();
            }
        });
        fab_quitar = findViewById(R.id.fab_quitar);
        fab_quitar.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                hideShowMenuFab();
                confirmQuitInadimplencia();
            }
        });
        fab_expand = findViewById(R.id.fab_menu_expand);
        fab_expand.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                hideShowMenuFab();
            }
        });

    }
    public void hideShowMenuFab(){
        if (visivel == true){
            fab_expand.setImageResource(R.drawable.ic_add);
            fab_produtos.hide();
            fab_quitar.hide();
            fab_nova_inad.hide();
            visivel = false;

            edittext_var = findViewById(R.id.title_quitar);
            edittext_var.setVisibility(View.INVISIBLE);

            edittext_var = findViewById(R.id.title_produtos);
            edittext_var.setVisibility(View.INVISIBLE);

            edittext_var = findViewById(R.id.title_nova_inad);
            edittext_var.setVisibility(View.INVISIBLE);
        } else{
            fab_expand.setImageResource(R.drawable.ic_close);
            fab_produtos.show();
            fab_quitar.show();
            fab_nova_inad.show();
            visivel = true;

            edittext_var = findViewById(R.id.title_quitar);
            edittext_var.setVisibility(View.VISIBLE);

            edittext_var = findViewById(R.id.title_produtos);
            edittext_var.setVisibility(View.VISIBLE);

            edittext_var = findViewById(R.id.title_nova_inad);
            edittext_var.setVisibility(View.VISIBLE);
        }
    }
    public void popupAtualizarCliente(){
        AlertDialog.Builder dialogBuilder;
        final AlertDialog dialog;
        dialogBuilder = new AlertDialog.Builder(this);
        final  View popupClienteView = getLayoutInflater().inflate(R.layout.popup_edit_cliente,null);
        dialogBuilder.setView(popupClienteView);
        dialog = dialogBuilder.create();
        dialog.setTitle("Editar cliente!");
        dialog.show();

        edt_nome = (EditText)popupClienteView.findViewById(R.id.popupNome);
        edt_email = (EditText)popupClienteView.findViewById(R.id.popupEmail);

        AdView adView = popupClienteView.findViewById(R.id.adViewBannerEdCliente);
        adView.loadAd(new AdRequest.Builder().build());

        edt_nome.setText(cliente.getNome());
        edt_email.setText(cliente.getEmail());
        Button add = popupClienteView.findViewById(R.id.btnSalvar);
        add.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                Cliente clienteEdit = new Cliente(cliente.getId(), edt_nome.getText().toString().trim(),
                        edt_email.getText().toString().trim());

                if (cliente.getNome().length() == 0 || cliente.getEmail().length() == 0) {
                    Toast.makeText(getApplicationContext(), "Preencha os campos, e tente novamente!", Toast.LENGTH_SHORT).show();
                } else if (cliente.getNome() ==  clienteEdit.getNome() && cliente.getEmail() == clienteEdit.getEmail()) {
                    Toast.makeText(getApplicationContext(),"Nenhum dado para atualizar!",Toast.LENGTH_SHORT).show();
                } else {
                    dialog.dismiss();

                    clienteDAO.atualizarCliente(clienteEdit);
                    cliente = clienteEdit;

                    Toast.makeText(getApplicationContext(),"Cliente atualizado com sucesso!",Toast.LENGTH_SHORT).show();

                }
            }
        });
    }
    public void confirmQuitInadimplencia(){
        AlertDialog dialog = new AlertDialog.Builder(this)
                .setMessage("Quitar inadimplência de : "+cliente.getNome()+" ?")
                .setPositiveButton("Sim", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        inadimplenciaDAO.setQuitInadimplencia(inadimplencia);
                        inadimplencia.setQuitada(true);
                        Toast.makeText(getApplicationContext(),"Inadimplência quitada com sucesso!",Toast.LENGTH_SHORT).show();
                    }

                })
                .setNegativeButton("Não", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        dialog.dismiss();
                    }
                }).show();
    }
    public void confirmNovaInadimplencia(){
        AlertDialog dialog = new AlertDialog.Builder(this)
                .setMessage("Iniciar nova inadimplência de : "+cliente.getNome()+" ?")
                .setPositiveButton("Sim", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        inadimplenciaDAO.deleteInadimplencia(inadimplencia);
                        Calendar calendar = Calendar.getInstance();
                        inadimplencia = new Inadimplencia(0, calendar,null, cliente, false, 0);
                        inadimplencia = inadimplenciaDAO.newInadimplencia(inadimplencia);
                        txt_total.setText("R$ Total");
                        txt_total.setTextColor(Color.BLACK);
                        txt_inicio.setText(converter.calendarToString(inadimplencia.getDataInicio()));
                        txt_pagamento.setText("");
                        Toast.makeText(getApplicationContext(),"Inadimplência reiniciada com sucesso!",Toast.LENGTH_SHORT).show();
                    }

                })
                .setNegativeButton("Não", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        dialog.dismiss();
                    }
                }).show();
    }
    public void confirmAddProdutosInad(){
        AlertDialog dialog = new AlertDialog.Builder(this)
                .setMessage("Para ter apenas o valor da inadimplência novamente sera necessario iniciar uma nova, " +
                        "deseja adicionar produtos na inadimplência de : "+cliente.getNome()+" ?")
                .setPositiveButton("Sim", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        if (inadimplencia.getTotal() <= 0 || inadimplencia.isQuitada()){
                            inadimplenciaDAO.deleteInadimplencia(inadimplencia);
                            Calendar calendar = Calendar.getInstance();
                            inadimplencia = new Inadimplencia(0,calendar,null,cliente, false, 0);
                            inadimplencia = inadimplenciaDAO.newInadimplencia(inadimplencia);
                            Intent intent = new Intent(ActivityInadimplencia.this, ProdutosActivity.class);
                            intent.putExtra("id", inadimplencia.getCliente().getId());
                            startActivity(intent);
                        } else {
                            Produto produto = new Produto(0,"Inadimplência",inadimplencia.getTotal(),1);
                            ProdutoDAO produtoDAO = new ProdutoDAO(ActivityInadimplencia.this);
                            produtoDAO.addProduto(inadimplencia.getId(), produto);
                            Intent intent = new Intent(ActivityInadimplencia.this, ProdutosActivity.class);
                            intent.putExtra("id", inadimplencia.getCliente().getId());
                            startActivity(intent);
                        }
                    }
                })
                .setNegativeButton("Não", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        dialog.dismiss();
                    }
                }).show();
    }
    public void dataPagamentoPopUp(){
        AlertDialog.Builder dialogBuilder;
        final AlertDialog dialog;
        dialogBuilder = new AlertDialog.Builder(this);
        final  View popupDataView = getLayoutInflater().inflate(R.layout.popup_data,null);
        dialogBuilder.setView(popupDataView);
        dialog = dialogBuilder.create();
        dialog.setTitle("Previsão para pagamento!");
        dialog.show();

        edt_data = popupDataView.findViewById(R.id.popup_data_edittext);
        AdView adView = popupDataView.findViewById(R.id.adViewBannerData);
        adView.loadAd(new AdRequest.Builder().build());

        SimpleMaskFormatter maskData = new SimpleMaskFormatter("[0-3][0-9]/[0-1][0-9]/[0-9][0-9][0-9][0-9]");
        MaskPattern maskPattern1 = new MaskPattern("[0-1]");
        MaskPattern maskPattern2 = new MaskPattern("[0-3]");
        MaskPattern maskPattern3 = new MaskPattern("[0-9]");
        maskData.registerPattern(maskPattern1);
        maskData.registerPattern(maskPattern2);
        maskData.registerPattern(maskPattern3);
        MaskTextWatcher maskTextWatcher = new MaskTextWatcher(edt_data, maskData);
        edt_data.addTextChangedListener(maskTextWatcher);

        final Button salvar = popupDataView.findViewById(R.id.btn_pupup_data_dalvar);

        if (inadimplencia.getDataFim() != null){
            edt_data.setText(converter.calendarToString(inadimplencia.getDataFim()));
            salvar.setText("Atualizar");
        }
        salvar.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (edt_data.getText().toString().length() == 10){
                    inadimplencia.setDataFim(converter.stringToCalendar(edt_data.getText().toString()));
                    inadimplenciaDAO.setDataPagamentoInadimplencia(inadimplencia);
                    Toast.makeText(getApplicationContext(),"Data salva com sucesso!"+ converter.calendarToString(inadimplencia.getDataFim()),Toast.LENGTH_SHORT).show();
                    txt_pagamento.setText(edt_data.getText().toString());
                    dialog.dismiss();
                } else {
                    Toast.makeText(getApplicationContext(),"Adicione uma data completa!"+edt_valor.getText().toString().length(),Toast.LENGTH_SHORT).show();
                }
                if (inadimplencia.getDataFim().getTime().before(Calendar.getInstance().getTime())){
                    txt_pagamento.setTextColor(Color.RED);
                } else {
                    txt_pagamento.setTextColor(Color.BLACK);
                }
            }
        });
    }
    public void gerarAdTelaToda(){
        interstitialAd = new InterstitialAd(this);
        interstitialAd.setAdUnitId("ca-app-pub-3940256099942544/1033173712");
        interstitialAd.loadAd(new AdRequest.Builder().build());
        interstitialAd.setAdListener(new AdListener(){
            @Override
            public void onAdLoaded() {
                interstitialAd.show();
            }
        });
    }
}
