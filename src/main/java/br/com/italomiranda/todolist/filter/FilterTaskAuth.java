package br.com.italomiranda.todolist.filter;

import at.favre.lib.crypto.bcrypt.BCrypt;
import br.com.italomiranda.todolist.user.IUserRepository;
import jakarta.servlet.*;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.util.Base64;

@Component
public class FilterTaskAuth extends OncePerRequestFilter{
    @Autowired
    private IUserRepository iUserRepository;

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain)
            throws ServletException, IOException {

        //Pega o caminho que está sendo feito a solicitação no request
        var servletPath = request.getServletPath();

        //se for http:localhost8080/tasks/ execute...
        if (servletPath.equals("/tasks/")) {
            //Pegar a autenticação (usuario e senha)
            var authorization = request.getHeader("Authorization");

            //recebe um Basic e12kdfswdlawod(codigo criptografado), realizando a retirada do "basic".
            var authEncoded = authorization.substring("Basic".length()).trim();

            //faz-se a decodificação desse codigo, em um array de bytes
            byte[] authDecode = Base64.getDecoder().decode(authEncoded);

            //Transforma esse array de bytes em string devolvendo isso username:password
            var authString = new String(authDecode);

            //transforma username:password em um array, como [username,password]
            String[] credentials = authString.split(":");
            String username = credentials[0];
            String password = credentials[1];

            //Valida se usuário existe no banco de dados, se não lança um response 401
            var user = this.iUserRepository.findByUsername(username);
            if(user == null){
                response.sendError(401);
            }else{
                //Descriptografa a senha e compara com a senha passada no auth request
                var passwordVerify =  BCrypt.verifyer().verify(password.toCharArray(),user.getPassword());

                //Verifica se ela esta correta
                if(passwordVerify.verified){
                    //acessa o request e seta no atributo idUser, o usuario que foi logado e achado no DB
                    request.setAttribute("idUser",user.getId());
                    //Segue viagem
                    filterChain.doFilter(request,response);
                }else{
                    response.sendError(401);
                }
            }
        }else{
            //Se nao for da rota (/tasks), segue viagem sem problemas
            filterChain.doFilter(request,response);
        }
    }
}
