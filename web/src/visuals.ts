export function foodEmoji(category: string) {
  const key = category.toLowerCase();
  return /pizza|بيتزا/.test(key) ? '🍕' : /burger|برجر/.test(key) ? '🍔' : /sweet|dessert|حلو/.test(key) ? '🍰' : /chicken|فراخ|دجاج/.test(key) ? '🍗' : '🥙';
}
