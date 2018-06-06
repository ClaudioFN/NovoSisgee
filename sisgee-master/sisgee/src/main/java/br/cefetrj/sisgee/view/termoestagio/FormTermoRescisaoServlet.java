package br.cefetrj.sisgee.view.termoestagio;

import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.Locale;
import java.util.ResourceBundle;

import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import br.cefetrj.sisgee.control.AlunoServices;
import br.cefetrj.sisgee.control.TermoEstagioServices;
import br.cefetrj.sisgee.model.entity.Aluno;
import br.cefetrj.sisgee.model.entity.TermoEstagio;
import br.cefetrj.sisgee.view.utils.ServletUtils;
import br.cefetrj.sisgee.view.utils.ValidaUtils;

/**
 * Servlet implementation class FormTermoRescisaoServlet
 */
@WebServlet("/FormTermoRescisaoServlet")
public class FormTermoRescisaoServlet extends HttpServlet {
	private static final long serialVersionUID = 1L;
       
	@Override
	protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {			
		
		request.getRequestDispatcher("/form_termo_rescisao.jsp").forward(request, response);
	}

	/**
	 * @see HttpServlet#doPost(HttpServletRequest request, HttpServletResponse response)
	 */
	@Override
	protected void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
		
		Locale locale = ServletUtils.getLocale(request);
		ResourceBundle messages = ResourceBundle.getBundle("Messages", locale);

		String dataTermoRescisao = request.getParameter("dataTermoRescisao");
		String idAluno = request.getParameter("idAluno");
		TermoEstagio termoEstagio = null;
		
		Aluno aluno = null;
		boolean isValid = true;		
		Date dataRescisao = null;
		String msg = "";
		
		/**
		 * Validação do Id do Aluno, usando métodos da Classe ValidaUtils.
		 * Instanciando o objeto e pegando o TermoEstagio válido (sem data de rescisão)
		 */
		String idAlunoMsg = "";
		idAlunoMsg = ValidaUtils.validaObrigatorio("Aluno", idAluno);
		if (idAlunoMsg.trim().isEmpty()) {
			idAlunoMsg = ValidaUtils.validaInteger("Aluno", idAluno);
			if (idAlunoMsg.trim().isEmpty()) {
				Integer idAlunoInt = Integer.parseInt(idAluno);
				aluno = AlunoServices.buscarAluno(new Aluno(idAlunoInt));
				if (aluno != null) {
					List<TermoEstagio> termosEstagio = aluno.getTermoEstagios();
					
					for (TermoEstagio termoEstagio2 : termosEstagio) {
						if(termoEstagio2.getDataRescisaoTermoEstagio() == null) {
							termoEstagio = termoEstagio2;
							break;
						}
					}					
					
				} else {
					idAlunoMsg = messages.getString("br.cefetrj.sisgee.incluir_termo_aditivo_servlet.msg_AlunoEscolhido");
					request.setAttribute("idAlunoMsg", idAlunoMsg);
				}

			} else {
				request.setAttribute("idAlunoMsg", idAlunoMsg);
				isValid = false;
			}
		} else {
			request.setAttribute("idAlunoMsg", idAlunoMsg);
			isValid = false;
		}		
		
		String dataTermoRescisaoMsg = "";
		
		String campo = "Termo Rescisão";
		dataTermoRescisaoMsg = ValidaUtils.validaObrigatorio(campo, dataTermoRescisao);
		if (dataTermoRescisaoMsg.trim().isEmpty()) {
			dataTermoRescisaoMsg = ValidaUtils.validaDate(campo, dataTermoRescisao);
			if (dataTermoRescisaoMsg.trim().isEmpty()) {				
				try {
					SimpleDateFormat format = new SimpleDateFormat("dd/MM/yyyy");
					dataRescisao = format.parse(dataTermoRescisao);
					request.setAttribute("dataRescisao", dataRescisao);
					
					if(termoEstagio != null) {			
						/**
						 * Validação do período (entre o início e fim do estágio) usando o método validaDatas da Classe ValidaUtils
						 */
						String periodoMsg = "";	
						
                                                periodoMsg = ValidaUtils.validaDatas(termoEstagio.getDataInicioTermoEstagio(), dataRescisao);
                                                if(!periodoMsg.trim().isEmpty()) {
                                                        periodoMsg = messages.getString(periodoMsg);
                                                        request.setAttribute("periodoMsg", periodoMsg);
                                                        isValid = false;					
                                                }	
							
						
					}else {
						msg = messages.getString("br.cefetrj.sisgee.form_termo_rescisao_servlet.msg_termo_estagio_invalido");
						isValid = false;
						request.setAttribute("termoEstagioMsg", msg);
					}
					
				} catch (Exception e) {
					//TODO trocar saída de console por Log
					System.out.println("Data em formato incorreto, mesmo após validação na classe ValidaUtils");
					isValid = false;
				}
			}else {
				dataTermoRescisaoMsg = messages.getString(dataTermoRescisaoMsg);
				request.setAttribute("dataTermoRescisaoMsg", dataTermoRescisaoMsg);
				isValid = false;
			}
		} else {
			dataTermoRescisaoMsg = messages.getString(dataTermoRescisaoMsg);
			request.setAttribute("dataTermoRescisaoMsg", dataTermoRescisaoMsg);
			isValid = false;
		}
		
		
		
		
		
		if (isValid) {
			termoEstagio.setDataRescisaoTermoEstagio(dataRescisao);
			TermoEstagioServices.alterarTermoEstagio(termoEstagio);
			msg = "Data de Rescisão registrada com sucesso";
			request.setAttribute("msg", msg);
			request.getRequestDispatcher("/index.jsp").forward(request, response);
			
			
		} else {			
			String rescisao = "sim";
			request.setAttribute("msg", msg);
			request.setAttribute("Rescisao", rescisao);
			
			request.getRequestDispatcher("/form_termo_rescisao.jsp").forward(request, response);
		}
	}

}