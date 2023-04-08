Projeto 1 foi refeito para ter as alterações que a professora queria
com o cálculo prévio do tamanho dos ficheiros. Para além disso modifiquei
também o código para estar mais "orientado a objetos", ou seja, está tudo
mais dividio, a usar as classes respetivas em vez de estar a salganhada
que estava antes com ficheiros a repetir código uns dos outros e com um
ficheiro grande que fazia as funcionalidades todas (*something*Operations.java)

Mais tarde faz-se o javadoc para se perceber tudo

Coloquei também uns javas que já tinha do projeto do ano passado com algumas partes
que pode dar para reutilizar

TODO:
1.Adaptar a classe PasswordFile para ter a funcionalidade de cálculo do MAC
(Exemplo de cálculo do MAC dum ficheiro está no ficheiro das TP)
2.Adaptar a classe PasswordFile para guarda a password hashed com salts.
3.Adaptar o cliente para fazer uso da classe ArgumentParser e corrigir eventuais bugs.
4.Adicionar à classe ArgumentParser os parametros -u -p e -au para funcionalidade dos utilizadore
5.Fazer um script de shell que crie rapidamente utilizadores (só pra poupar trabalho nos testes)
6.Adicionar a funcionalidade da verificação do MAC no arranque do servidor
7.Funcionalidade dos Canais Seguros: criar uma truststore pro cliente e colocar lá o certificado do servidor
(Exemplo com canais seguros está no ficheiro das TPs)
8.Adaptar/Criar uma classe para fazer gestão dos uploads dos ficheiros com um utilizador