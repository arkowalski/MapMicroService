function Repo(name,incoming,outgoing){
    this.name = name;
    this.incoming = incoming;
    this.outgoing = outgoing;

    function getIncoming(){
        return this.incoming;
    }
    function getOutgoing(){
        return this.outgoing;
    }

    function getName(){
        this.name;
    }
}


var a = new Repo("a",["b","c","d"],["c"]);
var b = new Repo("b",["d","c"],["a","c","d"]);
var c = new Repo("c",["a","b"],["a","b"]);
var d = new Repo("d",["b"],["a","b"]);


var listOfRepos= [a,b,c,d];

var linksList = [];
for(i = 0; i < listOfRepos.length; i++){
     var name = listOfRepos[i];
    print(name);
    //linksList.push({})
}