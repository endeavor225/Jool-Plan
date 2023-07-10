let text = "[lat/lng: (5.4026926851173975,-3.9603330446965863), lat/lng: (5.402848723216054,-3.9601271082325575), lat/lng: (5.403069625424366,-3.9603133001764657), lat/lng: (5.402903392182697,-3.9605326919250476)]"

let tableau = []

text = text.replace('[', '')
text = text.replace(']', '')
while (text.search('lat/lng: ') != -1){
    text = text.replace('lat/lng: ', '')
}
let tab = text.split(', ')

tab.forEach(element => {
    element = element.replace('(', '')
    element = element.replace(')', '')
    let tab2 = element.split(',')
    //console.table(tab2)

    for (let index = 0; index < tab2.length; index++) {
        tab2[index] = +tab2[index];
    }
    tableau.push(tab2)
});
console.table(tableau)
