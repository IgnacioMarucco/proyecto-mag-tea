export interface MchatPregunta {
  numero: number;
  texto: string;
  invertida: boolean;
}

export const MCHAT_PREGUNTAS: MchatPregunta[] = [
  { numero:  1, invertida: false, texto: 'Si Ud. señala algo que está del otro lado de la habitación, ¿su hijo o hija mira hacia allí? (Por ejemplo: si Ud. señala un juguete o un animal, ¿su hijo o hija mira al juguete o al animal?)' },
  { numero:  2, invertida: true,  texto: '¿Alguna vez se preguntó si su hijo o hija era sordo o sorda?' },
  { numero:  3, invertida: false, texto: '¿Su hijo o hija juega a simular, hacer "como si", o juegos de imaginación? (Por ejemplo: simula que toma de una taza vacía, finge hablar por teléfono, o hace como que le da de comer a una muñeca o a un peluche)' },
  { numero:  4, invertida: false, texto: '¿A su hijo o hija le gusta treparse a las cosas? (Por ejemplo: muebles, juegos de la plaza, o escaleras)' },
  { numero:  5, invertida: true,  texto: '¿Su hijo o hija hace movimientos raros con los dedos cerca de sus ojos? (Por ejemplo: ¿mueve o agita los dedos cerca de sus ojos de manera rara?)' },
  { numero:  6, invertida: false, texto: '¿Su hijo o hija señala con el dedo cuando quiere pedir algo o buscar ayuda? (Por ejemplo: señala algún alimento o juguete que está fuera de su alcance)' },
  { numero:  7, invertida: false, texto: '¿Su hijo o hija señala con el dedo cuando quiere mostrarle algo interesante? (Por ejemplo: señala un avión en el cielo o un camión muy grande en la calle)' },
  { numero:  8, invertida: false, texto: '¿Su hijo o hija se interesa por otros niños? (Por ejemplo: ¿mira a otros niños, les sonríe, se acerca a ellos?)' },
  { numero:  9, invertida: false, texto: '¿Su hijo o hija le muestra cosas, trayéndoselas o alzándolas para que Ud. las vea – no para buscar ayuda sino simplemente para compartirlas con Ud.? (Por ejemplo: le muestra una flor, un peluche, o un camión de juguete)' },
  { numero: 10, invertida: false, texto: '¿Su hijo o hija responde cuando lo/la llama por su nombre? (Por ejemplo: ¿su hijo o hija lo mira o la mira, habla o balbucea, o interrumpe lo que está haciendo cuando lo/la llama por su nombre?)' },
  { numero: 11, invertida: false, texto: 'Cuando le sonríe a su hijo o hija, ¿le devuelve la sonrisa?' },
  { numero: 12, invertida: true,  texto: '¿A su hijo o hija le molestan los ruidos comunes de todos los días? (Por ejemplo: ¿su hijo o hija grita o llora cuando escucha una aspiradora, una licuadora, una moto, la radio, música fuerte u otro ruido común?)' },
  { numero: 13, invertida: false, texto: '¿Su hijo o hija camina?' },
  { numero: 14, invertida: false, texto: '¿Su hijo o hija lo/la mira a los ojos cuando le está hablando, jugando con él/ella, o cuando lo/la está vistiendo?' },
  { numero: 15, invertida: false, texto: '¿Su hijo o hija trata de copiar lo que Ud. hace? (Por ejemplo: decir adiós con la mano, aplaudir, o hacer un ruido gracioso cuando Ud. lo hace)' },
  { numero: 16, invertida: false, texto: 'Si Ud. se da vuelta para mirar algo, ¿su hijo o hija gira la cabeza para ver lo que Ud. está mirando?' },
  { numero: 17, invertida: false, texto: '¿Su hijo o hija intenta hacer que Ud. lo/la mire? (Por ejemplo: ¿su hijo o hija lo/la mira para que lo/la felicite, o dice "mirá" o "mirame"?)' },
  { numero: 18, invertida: false, texto: '¿Su hijo o hija entiende cuando Ud. le dice que haga algo? (Por ejemplo: si Ud. no se lo señala, ¿su hijo o hija entiende cuando le pide "poné el libro sobre la silla" o "traeme la frazadita"?)' },
  { numero: 19, invertida: false, texto: 'Si pasa algo nuevo, ¿su hijo o hija lo/la mira a la cara para ver qué hace Ud.? (Por ejemplo: si su hijo o hija escucha un ruido raro o gracioso, o ve un juguete nuevo, ¿lo/la mira a la cara?)' },
  { numero: 20, invertida: false, texto: '¿A su hijo o hija le gustan las actividades de movimiento? (Por ejemplo: hamacarse o jugar al "caballito" sobre sus rodillas)' },
];
