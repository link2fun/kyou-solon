import { readdirSync, readFileSync } from 'node:fs';
import path from 'node:path';
import { describe, expect, it } from 'vitest';

/** 权限码字面量形态: system:user:list / monitor:job:changeStatus / system.group.add / common:upload */
const CODE_PATTERN =
  /\b(?:system|monitor|tool|common)[:.][A-Za-z][A-Za-z0-9]*(?:[.:][A-Za-z][A-Za-z0-9]*)*\b/g;

const ROOT = process.cwd();
const ARTIFACT_PATH = path.resolve(ROOT, '../permission-codes.txt');

/** 读取签入的权限码清单 */
const readKnownCodes = () =>
  new Set(
    readFileSync(ARTIFACT_PATH, 'utf8')
      .split('\n')
      .map((line) => line.trim())
      .filter((line) => line.length > 0 && !line.startsWith('#')),
  );

/** 递归收集待扫描的源码文件 */
const collectSourceFiles = (dir: string): string[] => {
  const files: string[] = [];
  for (const entry of readdirSync(dir, { withFileTypes: true })) {
    const fullPath = path.join(dir, entry.name);
    if (entry.isDirectory()) {
      if (
        entry.name === 'node_modules' ||
        entry.name.startsWith('.umi') ||
        entry.name === '__snapshots__'
      ) {
        continue;
      }
      files.push(...collectSourceFiles(fullPath));
      continue;
    }
    if (
      !/\.(ts|tsx)$/.test(entry.name) ||
      /\.(test|spec)\.(ts|tsx)$/.test(entry.name)
    ) {
      continue;
    }
    files.push(fullPath);
  }
  return files;
};

describe('权限码清单', () => {
  it('前端源码中的权限码字面量都存在于 permission-codes.txt', () => {
    const known = readKnownCodes();
    expect(known.size).toBeGreaterThan(0);

    const files = [
      path.join(ROOT, 'config/routes.ts'),
      ...collectSourceFiles(path.join(ROOT, 'src')),
    ];
    const unknown: string[] = [];
    for (const file of files) {
      const text = readFileSync(file, 'utf8');
      for (const match of text.matchAll(CODE_PATTERN)) {
        if (!known.has(match[0])) {
          unknown.push(`${path.relative(ROOT, file)}: ${match[0]}`);
        }
      }
    }

    expect(unknown).toEqual([]);
  });
});
